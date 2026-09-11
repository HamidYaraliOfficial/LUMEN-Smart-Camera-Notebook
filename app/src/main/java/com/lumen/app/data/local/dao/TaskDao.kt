package com.lumen.app.data.local.dao

import androidx.room.*
import com.lumen.app.data.local.entity.ContactEntity
import com.lumen.app.data.local.entity.EventEntity
import com.lumen.app.data.local.entity.ReminderEntity
import com.lumen.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueAtEpochMs ASC")
    fun observePending(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders ORDER BY remindAtEpochMs ASC")
    fun observeReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvent(event: EventEntity)

    @Query("SELECT * FROM events ORDER BY startAtEpochMs ASC")
    fun observeEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContact(contact: ContactEntity)

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun observeContacts(): Flow<List<ContactEntity>>
}
