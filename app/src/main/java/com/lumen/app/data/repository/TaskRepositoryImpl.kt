package com.lumen.app.data.repository

import com.lumen.app.data.local.dao.TaskDao
import com.lumen.app.data.local.entity.ContactEntity
import com.lumen.app.data.local.entity.EventEntity
import com.lumen.app.data.local.entity.ReminderEntity
import com.lumen.app.data.local.entity.TaskEntity
import com.lumen.app.domain.model.*
import com.lumen.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {
    override fun observePendingTasks(): Flow<List<LumenTask>> = dao.observePending().map { list ->
        list.map { LumenTask(it.id, it.title, it.notes, it.dueAtEpochMs, it.isCompleted, it.sourceDocumentId, it.sourceBlockId, it.createdAtEpochMs) }
    }

    override suspend fun upsertTask(task: LumenTask) = dao.upsert(
        TaskEntity(task.id, task.title, task.notes, task.dueAtEpochMs, task.isCompleted, task.sourceDocumentId, task.sourceBlockId, task.createdAtEpochMs)
    )

    override suspend fun setTaskCompleted(id: String, completed: Boolean) = dao.setCompleted(id, completed)

    override fun observeReminders(): Flow<List<LumenReminder>> = dao.observeReminders().map { list ->
        list.map { LumenReminder(it.id, it.title, it.remindAtEpochMs, it.sourceDocumentId, it.isSynced) }
    }

    override suspend fun upsertReminder(reminder: LumenReminder) = dao.upsertReminder(
        ReminderEntity(reminder.id, reminder.title, reminder.remindAtEpochMs, reminder.sourceDocumentId, reminder.isSynced)
    )

    override fun observeEvents(): Flow<List<LumenEvent>> = dao.observeEvents().map { list ->
        list.map { LumenEvent(it.id, it.title, it.startAtEpochMs, it.endAtEpochMs, it.location, it.sourceDocumentId, it.addedToCalendar) }
    }

    override suspend fun upsertEvent(event: LumenEvent) = dao.upsertEvent(
        EventEntity(event.id, event.title, event.startAtEpochMs, event.endAtEpochMs, event.location, event.sourceDocumentId, event.addedToCalendar)
    )

    override fun observeContacts(): Flow<List<LumenContact>> = dao.observeContacts().map { list ->
        list.map { LumenContact(it.id, it.name, it.company, it.jobTitle, it.phone, it.email, it.website, it.address, it.sourceDocumentId, it.savedToContacts) }
    }

    override suspend fun upsertContact(contact: LumenContact) = dao.upsertContact(
        ContactEntity(contact.id, contact.name, contact.company, contact.jobTitle, contact.phone, contact.email, contact.website, contact.address, contact.sourceDocumentId, contact.savedToContacts)
    )
}
