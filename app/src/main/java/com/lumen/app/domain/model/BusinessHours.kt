package com.lumen.app.domain.model

/** ISO-8601 day-of-week ordinal, 1 = Monday .. 7 = Sunday (matches java.time.DayOfWeek). */
data class DayHours(
    val dayOfWeek: Int,
    val isOpen: Boolean,
    val openMinuteOfDay: Int?,   // minutes since 00:00, e.g. 9:30 -> 570
    val closeMinuteOfDay: Int?,  // may be > 1440 to represent overnight closing (e.g. 25:30)
)

/**
 * User-entered operating-hours schedule (e.g. for a shop/office/service captured via LUMEN,
 * or for the user's own business). LUMEN computes live open/closed status and time-to-next-change
 * from this — nothing here is hardcoded, it is entirely user-provided.
 */
data class BusinessHoursSchedule(
    val id: String,
    val label: String,
    val timeZoneId: String,           // e.g. "Asia/Baku"
    val days: List<DayHours>,         // exactly 7 entries, one per ISO day-of-week
    val updatedAtEpochMs: Long,
)

data class BusinessOpenStatus(
    val isOpenNow: Boolean,
    val currentLocalTimeLabel: String,
    val nextChangeLabel: String,       // e.g. "Closes at 18:00" or "Opens at 09:00"
    val minutesUntilNextChange: Long,
    val durationUntilNextChangeLabel: String, // e.g. "2h 15m"
)
