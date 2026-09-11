package com.lumen.app.ui.businesshours

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumen.app.core.util.DateUtils
import com.lumen.app.domain.model.BusinessHoursSchedule
import com.lumen.app.ui.components.EmptyState
import com.lumen.app.ui.components.LumenCard
import com.lumen.app.ui.components.LumenTopBar
import java.time.ZoneId

private val DAY_NAMES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

/**
 * Fully user-driven Business Hours editor. Nothing here is pre-populated: the user names the
 * schedule, picks a time zone, and taps each day to set whether it's open and its open/close
 * times. LUMEN then shows a live "Open now / Closed" pill with the exact duration until the next
 * change, recomputed from those inputs.
 */
@Composable
fun BusinessHoursScreen(
    onBack: () -> Unit,
    viewModel: BusinessHoursViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LumenTopBar(title = "Business hours", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) { Icon(Icons.Filled.Add, null) }
        }
    ) { padding ->
        if (state.schedules.isEmpty()) {
            EmptyState(
                title = "No schedules yet",
                subtitle = "Add a schedule and enter opening hours for each day — LUMEN will work out the rest.",
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.schedules, key = { it.id }) { schedule ->
                    ScheduleCard(schedule = schedule, status = state.statusByScheduleId[schedule.id], viewModel = viewModel)
                }
            }
        }

        if (showCreateDialog) {
            CreateScheduleDialog(
                defaultZone = ZoneId.systemDefault().id,
                onDismiss = { showCreateDialog = false },
                onCreate = { label, zone ->
                    viewModel.createSchedule(label, zone)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
private fun ScheduleCard(schedule: BusinessHoursSchedule, status: com.lumen.app.domain.model.BusinessOpenStatus?, viewModel: BusinessHoursViewModel) {
    var expanded by remember { mutableStateOf(false) }

    LumenCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(schedule.label, fontWeight = FontWeight.SemiBold)
                Text(schedule.timeZoneId, style = MaterialTheme.typography.bodySmall)
            }
            if (status != null) {
                StatusPill(isOpen = status.isOpenNow)
            }
        }

        status?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                "${it.nextChangeLabel} · in ${it.durationUntilNextChangeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide days" else "Edit days") }

        if (expanded) {
            schedule.days.sortedBy { it.dayOfWeek }.forEach { day ->
                DayEditorRow(
                    dayLabel = DAY_NAMES[day.dayOfWeek - 1],
                    isOpen = day.isOpen,
                    openLabel = day.openMinuteOfDay?.let { DateUtils.minutesOfDayToLabel(it) } ?: "09:00",
                    closeLabel = day.closeMinuteOfDay?.let { DateUtils.minutesOfDayToLabel(it % (24 * 60)) } ?: "18:00",
                    onChange = { isOpen, open, close -> viewModel.updateDay(schedule, day.dayOfWeek, isOpen, open, close) },
                )
            }
        }
    }
}

@Composable
private fun StatusPill(isOpen: Boolean) {
    val color = if (isOpen) com.lumen.app.core.theme.ConfidenceHigh else com.lumen.app.core.theme.ConfidenceLow
    Surface(color = color.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
        Text(
            text = if (isOpen) "Open now" else "Closed",
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun DayEditorRow(
    dayLabel: String,
    isOpen: Boolean,
    openLabel: String,
    closeLabel: String,
    onChange: (Boolean, String, String) -> Unit,
) {
    var open by remember(isOpen) { mutableStateOf(isOpen) }
    var openTime by remember(openLabel) { mutableStateOf(openLabel) }
    var closeTime by remember(closeLabel) { mutableStateOf(closeLabel) }

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(dayLabel, modifier = Modifier.width(48.dp))
        Switch(checked = open, onCheckedChange = { open = it; onChange(it, openTime, closeTime) })
        if (open) {
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = openTime,
                onValueChange = { openTime = it; onChange(open, it, closeTime) },
                modifier = Modifier.width(90.dp),
                label = { Text("Open") },
                singleLine = true,
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = closeTime,
                onValueChange = { closeTime = it; onChange(open, openTime, it) },
                modifier = Modifier.width(90.dp),
                label = { Text("Close") },
                singleLine = true,
            )
        } else {
            Spacer(Modifier.width(8.dp))
            Text("Closed all day", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CreateScheduleDialog(defaultZone: String, onDismiss: () -> Unit, onCreate: (String, String) -> Unit) {
    var label by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf(defaultZone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New schedule") },
        text = {
            Column {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (e.g. My Shop)") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = zone, onValueChange = { zone = it }, label = { Text("Time zone (e.g. Asia/Baku)") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (label.isNotBlank()) onCreate(label, zone.ifBlank { defaultZone }) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
