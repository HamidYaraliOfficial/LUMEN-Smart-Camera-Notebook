package com.lumen.app.data.repository

import com.lumen.app.data.local.dao.DocumentDao
import com.lumen.app.data.local.dao.NoteDao
import com.lumen.app.data.local.dao.ReceiptDao
import com.lumen.app.domain.repository.SearchRepository
import com.lumen.app.domain.repository.SearchResults
import javax.inject.Inject

/** Local Search Engine: queries Room FTS4 shadow tables for notes/documents plus a LIKE-based
 * scan for receipts (their fields are more structured/short so FTS adds little). */
class SearchRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val documentDao: DocumentDao,
    private val receiptDao: ReceiptDao,
) : SearchRepository {
    override suspend fun searchAll(query: String): SearchResults {
        if (query.isBlank()) return SearchResults(emptyList(), emptyList(), emptyList())
        val ftsQuery = "${query.trim()}*"
        val notes = runCatching { noteDao.searchFts(ftsQuery) }.getOrDefault(emptyList()).map { it.toDomain() }
        val documents = runCatching { documentDao.searchFts(ftsQuery) }.getOrDefault(emptyList()).map { it.toDomain() }
        val receipts = runCatching { receiptDao.search(query) }.getOrDefault(emptyList()).map { it.toDomain(emptyList()) }
        return SearchResults(notes, documents, receipts)
    }
}
