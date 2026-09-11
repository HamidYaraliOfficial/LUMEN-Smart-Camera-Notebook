package com.lumen.app.core.util

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    fun formatEpochMs(epochMs: Long, pattern: String = "yyyy-MM-dd HH:mm", zoneId: String? = null, locale: Locale = Locale.getDefault()): String {
        val zone = zoneId?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
        return Instant.ofEpochMilli(epochMs).atZone(zone).format(formatter)
    }

    fun minutesOfDayToLabel(minutes: Int): String {
        val h = (minutes / 60) % 24
        val m = minutes % 60
        return String.format(Locale.US, "%02d:%02d", h, m)
    }

    fun labelToMinutesOfDay(label: String): Int? {
        val parts = label.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return h * 60 + m
    }

    fun durationLabel(totalMinutes: Long): String {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }
}
