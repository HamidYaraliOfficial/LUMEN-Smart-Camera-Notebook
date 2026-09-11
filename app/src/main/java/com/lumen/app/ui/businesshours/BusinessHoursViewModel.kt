package com.lumen.app.ui.businesshours

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.app.core.util.DateUtils
import com.lumen.app.domain.model.BusinessHoursSchedule
import com.lumen.app.domain.model.BusinessOpenStatus
import com.lumen.app.domain.model.DayHours
import com.lumen.app.domain.repository.BusinessHoursRepository
import com.lumen.app.domain.usecase.CalculateBusinessStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

data class BusinessHoursUiState(
    val schedules: List<BusinessHoursSchedule> = emptyList(),
    val statusByScheduleId: Map<String, BusinessOpenStatus> = emptyMap(),
)

/**
 * Backs the fully user-editable "Business Hours" settings screen: the user types in a label,
 * time zone, and per-day open/close times; LUMEN then computes live open/closed status and the
 * exact duration remaining until the next change — entirely from what was entered, nothing
 * pre-filled or hardcoded.
 */
@HiltViewModel
class BusinessHoursViewModel @Inject constructor(
    private val repository: BusinessHoursRepository,
    private val calculateStatus: CalculateBusinessStatusUseCase,
) : ViewModel() {

    val uiState: StateFlow<BusinessHoursUiState> = repository.observeSchedules()
        .map { schedules ->
            val statuses = schedules.associate { it.id to runCatching { calculateStatus(it) }.getOrNull() }
                .filterValues { it != null }
                .mapValues { it.value!! }
            BusinessHoursUiState(schedules, statuses)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessHoursUiState())

    fun createSchedule(label: String, timeZoneId: String) {
        viewModelScope.launch {
            val days = (1..7).map { dow -> DayHours(dow, isOpen = false, openMinuteOfDay = null, closeMinuteOfDay = null) }
            repository.upsert(
                BusinessHoursSchedule(
                    id = UUID.randomUUID().toString(),
                    label = label,
                    timeZoneId = timeZoneId,
                    days = days,
                    updatedAtEpochMs = System.currentTimeMillis(),
                )
            )
        }
    }

    fun updateDay(schedule: BusinessHoursSchedule, dayOfWeek: Int, isOpen: Boolean, openLabel: String, closeLabel: String) {
        viewModelScope.launch {
            val openMinutes = DateUtils.labelToMinutesOfDay(openLabel)
            val closeMinutesRaw = DateUtils.labelToMinutesOfDay(closeLabel)
            // Support overnight closing times (e.g. open 18:00, close 02:00) by rolling the close
            // time into the next day when it's numerically earlier than the open time.
            val closeMinutes = if (openMinutes != null && closeMinutesRaw != null && closeMinutesRaw <= openMinutes) {
                closeMinutesRaw + 24 * 60
            } else closeMinutesRaw

            val updatedDays = schedule.days.map { day ->
                if (day.dayOfWeek == dayOfWeek) day.copy(isOpen = isOpen, openMinuteOfDay = openMinutes, closeMinuteOfDay = closeMinutes)
                else day
            }
            repository.upsert(schedule.copy(days = updatedDays, updatedAtEpochMs = System.currentTimeMillis()))
        }
    }

    fun deleteSchedule(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun availableTimeZones(): List<String> = ZoneId.getAvailableZoneIds().sorted()
}
