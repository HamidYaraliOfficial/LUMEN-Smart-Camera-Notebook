package com.lumen.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String?,
    val dueAtEpochMs: Long?,
    val isCompleted: Boolean,
    val sourceDocumentId: String?,
    val sourceBlockId: String?,
    val createdAtEpochMs: Long,
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val remindAtEpochMs: Long,
    val sourceDocumentId: String?,
    val isSynced: Boolean,
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startAtEpochMs: Long,
    val endAtEpochMs: Long?,
    val location: String?,
    val sourceDocumentId: String?,
    val addedToCalendar: Boolean,
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val company: String?,
    val jobTitle: String?,
    val phone: String?,
    val email: String?,
    val website: String?,
    val address: String?,
    val sourceDocumentId: String?,
    val savedToContacts: Boolean,
)
