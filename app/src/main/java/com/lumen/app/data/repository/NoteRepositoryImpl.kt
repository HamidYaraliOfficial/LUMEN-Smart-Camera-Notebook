package com.lumen.app.data.repository

import com.lumen.app.data.local.dao.NoteDao
import com.lumen.app.data.local.entity.NoteVersionEntity
import com.lumen.app.domain.model.Note
import com.lumen.app.domain.model.NoteVersion
import com.lumen.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {
    override fun observeByNotebook(notebookId: String): Flow<List<Note>> =
        dao.observeByNotebook(notebookId).map { list -> list.map { it.toDomain() } }

    override fun observeFavorites(): Flow<List<Note>> = dao.observeFavorites().map { list -> list.map { it.toDomain() } }
    override fun observeRecent(limit: Int): Flow<List<Note>> = dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }
    override fun observeById(id: String): Flow<Note?> = dao.observeById(id).map { it?.toDomain() }

    override suspend fun upsert(note: Note, recordVersion: Boolean) {
        dao.upsert(note.toEntity())
        if (recordVersion) {
            dao.insertVersion(
                NoteVersionEntity(
                    id = UUID.randomUUID().toString(),
                    noteId = note.id,
                    versionNumber = note.version,
                    bodyMarkdownSnapshot = note.bodyMarkdown,
                    editedAtEpochMs = System.currentTimeMillis(),
                )
            )
        }
    }

    override suspend fun delete(id: String) = dao.delete(id)

    override suspend fun toggleFavorite(id: String, favorite: Boolean) {
        val existing = dao.getById(id) ?: return
        dao.upsert(existing.copy(isFavorite = favorite, updatedAtEpochMs = System.currentTimeMillis()))
    }

    override fun observeVersions(noteId: String): Flow<List<NoteVersion>> =
        dao.observeVersions(noteId).map { list -> list.map { it.toDomain() } }
}
