package com.lumen.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User-entered operating hours (e.g. "my shop", "clinic I scanned the sign of", etc).
 * Nothing is pre-populated — the app derives open/closed + time-to-next-change purely
 * from whatever the user types in Settings -> Business Hours.
 */
@Entity(tableName = "business_hours_schedules")
data class BusinessHoursScheduleEntity(
    @PrimaryKey val id: String,
    val label: String,
    val timeZoneId: String,
    val updatedAtEpochMs: Long,
)

@Entity(tableName = "business_hours_days")
data class DayHoursEntity(
    @PrimaryKey(autoGenerate = true) val autoId: Long = 0,
    val scheduleId: String,
    val dayOfWeek: Int, // 1=Monday..7=Sunday
    val isOpen: Boolean,
    val openMinuteOfDay: Int?,
    val closeMinuteOfDay: Int?,
)
