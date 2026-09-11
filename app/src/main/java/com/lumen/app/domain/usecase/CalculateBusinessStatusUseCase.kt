package com.lumen.app.domain.usecase

import com.lumen.app.core.util.DateUtils
import com.lumen.app.domain.model.BusinessHoursSchedule
import com.lumen.app.domain.model.BusinessOpenStatus
import com.lumen.app.domain.model.DayHours
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Computes live "open now / closed" status and time-until-next-change purely from the
 * user-entered [BusinessHoursSchedule] (see Settings -> Business Hours). Nothing about opening
 * hours is hardcoded anywhere in LUMEN — every schedule, every day's open/close time, and the
 * time zone are all values the user typed in.
 */
class CalculateBusinessStatusUseCase @Inject constructor() {

    operator fun invoke(schedule: BusinessHoursSchedule, now: ZonedDateTime = ZonedDateTime.now(zoneOf(schedule))): BusinessOpenStatus {
        val zone = zoneOf(schedule)
        val nowInZone = now.withZoneSameInstant(zone)
        val nowMinuteOfWeek = minuteOfWeek(nowInZone)

        val intervals = buildWeekIntervals(schedule)
        val activeInterval = intervals.firstOrNull { nowMinuteOfWeek in it.first until it.second }

        return if (activeInterval != null) {
            val minutesUntilClose = activeInterval.second - nowMinuteOfWeek
            BusinessOpenStatus(
                isOpenNow = true,
                currentLocalTimeLabel = DateUtils.formatEpochMs(nowInZone.toInstant().toEpochMilli(), "HH:mm", schedule.timeZoneId),
                nextChangeLabel = "Closes at ${minuteOfWeekToClock(activeInterval.second)}",
                minutesUntilNextChange = minutesUntilClose.toLong(),
                durationUntilNextChangeLabel = DateUtils.durationLabel(minutesUntilClose.toLong()),
            )
        } else {
            val nextOpen = intervals
                .map { if (it.first >= nowMinuteOfWeek) it.first else it.first + MINUTES_PER_WEEK }
                .minOrNull()
            val minutesUntilOpen = (nextOpen ?: (nowMinuteOfWeek + MINUTES_PER_WEEK)) - nowMinuteOfWeek
            BusinessOpenStatus(
                isOpenNow = false,
                currentLocalTimeLabel = DateUtils.formatEpochMs(nowInZone.toInstant().toEpochMilli(), "HH:mm", schedule.timeZoneId),
                nextChangeLabel = if (nextOpen != null) "Opens at ${minuteOfWeekToClock(nextOpen % MINUTES_PER_WEEK)}" else "No upcoming hours set",
                minutesUntilNextChange = minutesUntilOpen.toLong(),
                durationUntilNextChangeLabel = DateUtils.durationLabel(minutesUntilOpen.toLong()),
            )
        }
    }

    /** Expands each [DayHours] into an absolute [minuteOfWeek, minuteOfWeek) interval, handling
     * overnight closes (closeMinuteOfDay > 1440) by letting the interval spill into the next day. */
    private fun buildWeekIntervals(schedule: BusinessHoursSchedule): List<Pair<Int, Int>> =
        schedule.days.filter { it.isOpen && it.openMinuteOfDay != null && it.closeMinuteOfDay != null }
            .map { day ->
                val dayOffset = (day.dayOfWeek - 1) * MINUTES_PER_DAY
                val open = dayOffset + day.openMinuteOfDay!!
                val close = dayOffset + day.closeMinuteOfDay!!
                open to close
            }

    private fun minuteOfWeek(dateTime: ZonedDateTime): Int {
        val dow = dateTime.dayOfWeek.value // 1..7, Monday..Sunday
        return (dow - 1) * MINUTES_PER_DAY + dateTime.hour * 60 + dateTime.minute
    }

    private fun minuteOfWeekToClock(minuteOfWeek: Int): String {
        val minuteOfDay = minuteOfWeek % MINUTES_PER_DAY
        return DateUtils.minutesOfDayToLabel(minuteOfDay)
    }

    private fun zoneOf(schedule: BusinessHoursSchedule): ZoneId =
        runCatching { ZoneId.of(schedule.timeZoneId) }.getOrDefault(ZoneId.systemDefault())

    companion object {
        private const val MINUTES_PER_DAY = 24 * 60
        private const val MINUTES_PER_WEEK = 7 * MINUTES_PER_DAY
    }
}
