package com.lumen.app.data.repository

import com.lumen.app.data.local.dao.BusinessHoursDao
import com.lumen.app.domain.model.BusinessHoursSchedule
import com.lumen.app.domain.repository.BusinessHoursRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BusinessHoursRepositoryImpl @Inject constructor(
    private val dao: BusinessHoursDao
) : BusinessHoursRepository {

    override fun observeSchedules(): Flow<List<BusinessHoursSchedule>> =
        dao.observeSchedules().map { schedules -> schedules.map { it.toDomain(emptyList()) } }

    override fun observeSchedule(id: String): Flow<BusinessHoursSchedule?> =
        dao.observeSchedule(id).combine(dao.observeDays(id)) { schedule, days ->
            schedule?.toDomain(days.map { it.toDomain() })
        }

    override suspend fun upsert(schedule: BusinessHoursSchedule) {
        dao.upsertSchedule(schedule.toEntity())
        dao.clearDays(schedule.id)
        dao.insertDays(schedule.days.map { it.toEntity(schedule.id) })
    }

    override suspend fun delete(id: String) = dao.deleteSchedule(id)
}
