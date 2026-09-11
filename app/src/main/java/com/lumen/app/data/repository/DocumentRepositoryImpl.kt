package com.lumen.app.data.repository

import com.lumen.app.data.local.dao.DocumentDao
import com.lumen.app.domain.model.DocumentPage
import com.lumen.app.domain.model.LumenDocument
import com.lumen.app.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val dao: DocumentDao
) : DocumentRepository {

    override fun observeByNotebook(notebookId: String): Flow<List<LumenDocument>> =
        dao.observeByNotebook(notebookId).map { list -> list.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<LumenDocument>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<LumenDocument?> =
        dao.observeById(id).combine(dao.observePages(id)) { doc, pages ->
            doc?.toDomain(pages.map { it.toDomain() })
        }

    override suspend fun getById(id: String): LumenDocument? = dao.getById(id)?.toDomain()

    override suspend fun upsert(document: LumenDocument) {
        dao.upsert(document.toEntity())
        document.pages.forEach { dao.upsertPage(it.toEntity(document.id)) }
    }

    override suspend fun upsertPage(documentId: String, page: DocumentPage) {
        dao.upsertPage(page.toEntity(documentId))
    }

    override fun observePages(documentId: String): Flow<List<DocumentPage>> =
        dao.observePages(documentId).map { list -> list.map { it.toDomain() } }

    override suspend fun deletePage(pageId: String) = dao.deletePage(pageId)
    override suspend fun reorderPage(pageId: String, newIndex: Int) = dao.reorderPage(pageId, newIndex)
    override suspend fun delete(id: String) = dao.delete(id)
}
