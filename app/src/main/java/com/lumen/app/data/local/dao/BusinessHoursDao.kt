package com.lumen.app.data.local.dao

import androidx.room.*
import com.lumen.app.data.local.entity.BusinessHoursScheduleEntity
import com.lumen.app.data.local.entity.DayHoursEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessHoursDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(schedule: BusinessHoursScheduleEntity)

    @Query("DELETE FROM business_hours_days WHERE scheduleId = :scheduleId")
    suspend fun clearDays(scheduleId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<DayHoursEntity>)

    @Query("SELECT * FROM business_hours_schedules ORDER BY updatedAtEpochMs DESC")
    fun observeSchedules(): Flow<List<BusinessHoursScheduleEntity>>

    @Query("SELECT * FROM business_hours_schedules WHERE id = :id")
    fun observeSchedule(id: String): Flow<BusinessHoursScheduleEntity?>

    @Query("SELECT * FROM business_hours_days WHERE scheduleId = :scheduleId ORDER BY dayOfWeek ASC")
    fun observeDays(scheduleId: String): Flow<List<DayHoursEntity>>

    @Query("SELECT * FROM business_hours_days WHERE scheduleId = :scheduleId ORDER BY dayOfWeek ASC")
    suspend fun getDays(scheduleId: String): List<DayHoursEntity>

    @Query("DELETE FROM business_hours_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: String)
}
