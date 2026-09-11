package com.lumen.app.data.repository

import com.lumen.app.data.local.dao.NotebookDao
import com.lumen.app.domain.model.Notebook
import com.lumen.app.domain.repository.NotebookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class NotebookRepositoryImpl @Inject constructor(
    private val dao: NotebookDao
) : NotebookRepository {
    override fun observeActive(): Flow<List<Notebook>> = dao.observeActive().map { list -> list.map { it.toDomain() } }
    override fun observeArchived(): Flow<List<Notebook>> = dao.observeArchived().map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: String): Notebook? = dao.getById(id)?.toDomain()

    override suspend fun create(name: String, colorHex: String, icon: String): Notebook {
        val now = System.currentTimeMillis()
        val notebook = Notebook(UUID.randomUUID().toString(), name, colorHex, icon, createdAtEpochMs = now, updatedAtEpochMs = now)
        dao.upsert(notebook.toEntity())
        return notebook
    }

    override suspend fun rename(id: String, name: String) {
        val existing = dao.getById(id) ?: return
        dao.update(existing.copy(name = name, updatedAtEpochMs = System.currentTimeMillis()))
    }

    override suspend fun delete(id: String) = dao.delete(id)
    override suspend fun setPinned(id: String, pinned: Boolean) = dao.setPinned(id, pinned)
    override suspend fun setArchived(id: String, archived: Boolean) = dao.setArchived(id, archived)
    override suspend fun setLocked(id: String, locked: Boolean) = dao.setLocked(id, locked)
}
