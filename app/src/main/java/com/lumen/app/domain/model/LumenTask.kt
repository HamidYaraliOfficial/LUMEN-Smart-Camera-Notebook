package com.lumen.app.domain.model

data class LumenTask(
    val id: String,
    val title: String,
    val notes: String?,
    val dueAtEpochMs: Long?,
    val isCompleted: Boolean = false,
    val sourceDocumentId: String?,
    val sourceBlockId: String?,
    val createdAtEpochMs: Long,
)

data class LumenReminder(
    val id: String,
    val title: String,
    val remindAtEpochMs: Long,
    val sourceDocumentId: String?,
    val isSynced: Boolean = false,
)

data class LumenEvent(
    val id: String,
    val title: String,
    val startAtEpochMs: Long,
    val endAtEpochMs: Long?,
    val location: String?,
    val sourceDocumentId: String?,
    val addedToCalendar: Boolean = false,
)

data class LumenContact(
    val id: String,
    val name: String?,
    val company: String?,
    val jobTitle: String?,
    val phone: String?,
    val email: String?,
    val website: String?,
    val address: String?,
    val sourceDocumentId: String?,
    val savedToContacts: Boolean = false,
)
