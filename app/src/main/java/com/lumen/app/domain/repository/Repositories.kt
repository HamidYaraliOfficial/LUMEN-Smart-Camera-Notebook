package com.lumen.app.domain.repository

import com.lumen.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface NotebookRepository {
    fun observeActive(): Flow<List<Notebook>>
    fun observeArchived(): Flow<List<Notebook>>
    suspend fun getById(id: String): Notebook?
    suspend fun create(name: String, colorHex: String, icon: String): Notebook
    suspend fun rename(id: String, name: String)
    suspend fun delete(id: String)
    suspend fun setPinned(id: String, pinned: Boolean)
    suspend fun setArchived(id: String, archived: Boolean)
    suspend fun setLocked(id: String, locked: Boolean)
}

interface NoteRepository {
    fun observeByNotebook(notebookId: String): Flow<List<Note>>
    fun observeFavorites(): Flow<List<Note>>
    fun observeRecent(limit: Int = 20): Flow<List<Note>>
    fun observeById(id: String): Flow<Note?>
    suspend fun upsert(note: Note, recordVersion: Boolean = true)
    suspend fun delete(id: String)
    suspend fun toggleFavorite(id: String, favorite: Boolean)
    fun observeVersions(noteId: String): Flow<List<NoteVersion>>
}

interface DocumentRepository {
    fun observeByNotebook(notebookId: String): Flow<List<LumenDocument>>
    fun observeRecent(limit: Int = 20): Flow<List<LumenDocument>>
    fun observeById(id: String): Flow<LumenDocument?>
    suspend fun getById(id: String): LumenDocument?
    suspend fun upsert(document: LumenDocument)
    suspend fun upsertPage(documentId: String, page: DocumentPage)
    fun observePages(documentId: String): Flow<List<DocumentPage>>
    suspend fun deletePage(pageId: String)
    suspend fun reorderPage(pageId: String, newIndex: Int)
    suspend fun delete(id: String)
}

interface OcrRepository {
    suspend fun save(result: OcrResult)
    suspend fun getResult(id: String): OcrResult?
    fun observeResult(id: String): Flow<OcrResult?>
    fun observeBlocks(ocrResultId: String): Flow<List<OcrTextBlock>>
}

interface ReceiptRepository {
    fun observeByNotebook(notebookId: String): Flow<List<Receipt>>
    fun observeRecent(limit: Int = 20): Flow<List<Receipt>>
    fun observeById(id: String): Flow<Receipt?>
    suspend fun upsert(receipt: Receipt)
    suspend fun delete(id: String)
    suspend fun search(query: String): List<Receipt>
}

interface TableRepository {
    suspend fun upsert(table: LumenDataTable)
    fun observeByDocument(documentId: String): Flow<List<LumenDataTable>>
    fun observeColumns(tableId: String): Flow<List<TableColumn>>
    fun observeRows(tableId: String): Flow<List<TableRow>>
    suspend fun updateCell(rowId: String, columnId: String, value: String)
    suspend fun deleteRow(rowId: String)
    suspend fun deleteColumn(columnId: String)
}

interface TaskRepository {
    fun observePendingTasks(): Flow<List<LumenTask>>
    suspend fun upsertTask(task: LumenTask)
    suspend fun setTaskCompleted(id: String, completed: Boolean)
    fun observeReminders(): Flow<List<LumenReminder>>
    suspend fun upsertReminder(reminder: LumenReminder)
    fun observeEvents(): Flow<List<LumenEvent>>
    suspend fun upsertEvent(event: LumenEvent)
    fun observeContacts(): Flow<List<LumenContact>>
    suspend fun upsertContact(contact: LumenContact)
}

interface StudyRepository {
    fun observeFlashcards(notebookId: String): Flow<List<Flashcard>>
    fun observeAllFlashcards(): Flow<List<Flashcard>>
    suspend fun upsertFlashcards(cards: List<Flashcard>)
    suspend fun markReviewed(id: String)
    suspend fun delete(id: String)
}

interface SearchRepository {
    suspend fun searchAll(query: String): SearchResults
}

data class SearchResults(
    val notes: List<Note>,
    val documents: List<LumenDocument>,
    val receipts: List<Receipt>,
)

interface BusinessHoursRepository {
    fun observeSchedules(): Flow<List<BusinessHoursSchedule>>
    fun observeSchedule(id: String): Flow<BusinessHoursSchedule?>
    suspend fun upsert(schedule: BusinessHoursSchedule)
    suspend fun delete(id: String)
}
