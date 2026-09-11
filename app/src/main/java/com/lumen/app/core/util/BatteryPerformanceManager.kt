package com.lumen.app.core.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import javax.inject.Inject
import javax.inject.Singleton

enum class PerformanceTier { HIGH, BALANCED, LOW_POWER }

data class DevicePerformanceState(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val isThermalThrottled: Boolean,
    val tier: PerformanceTier,
)

/**
 * Real battery/thermal reader used to drive adaptive behaviour: analysis frame interval
 * (CameraController.setAnalysisIntervalMs), OCR/inference resolution, and whether batch jobs
 * should pause. Uses the real Android BatteryManager + PowerManager thermal-status APIs.
 */
@Singleton
class BatteryPerformanceManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    fun currentState(): DevicePerformanceState {
        val batteryIntent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val isThrottled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
            (pm?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE) >= PowerManager.THERMAL_STATUS_MODERATE
        } else false

        val tier = when {
            isThrottled || (percent in 1..15 && !isCharging) -> PerformanceTier.LOW_POWER
            percent in 16..35 && !isCharging -> PerformanceTier.BALANCED
            else -> PerformanceTier.HIGH
        }

        return DevicePerformanceState(percent, isCharging, isThrottled, tier)
    }

    /** Recommended Live-OCR analysis interval in ms for the current tier. */
    fun recommendedAnalysisIntervalMs(): Long = when (currentState().tier) {
        PerformanceTier.HIGH -> 400L
        PerformanceTier.BALANCED -> 800L
        PerformanceTier.LOW_POWER -> 1800L
    }

    /** Recommended max parallel batch-scan workers for the current tier. */
    fun recommendedParallelJobs(): Int = when (currentState().tier) {
        PerformanceTier.HIGH -> 3
        PerformanceTier.BALANCED -> 2
        PerformanceTier.LOW_POWER -> 1
    }
}
