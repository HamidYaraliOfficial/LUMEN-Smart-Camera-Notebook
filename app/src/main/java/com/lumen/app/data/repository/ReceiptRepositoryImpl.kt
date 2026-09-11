package com.lumen.app.data.repository

import com.lumen.app.data.local.dao.ReceiptDao
import com.lumen.app.domain.model.Receipt
import com.lumen.app.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReceiptRepositoryImpl @Inject constructor(
    private val dao: ReceiptDao
) : ReceiptRepository {
    override fun observeByNotebook(notebookId: String): Flow<List<Receipt>> =
        dao.observeByNotebook(notebookId).map { list -> list.map { it.toDomain(emptyList()) } }

    override fun observeRecent(limit: Int): Flow<List<Receipt>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain(emptyList()) } }

    override fun observeById(id: String): Flow<Receipt?> =
        dao.observeById(id).combine(dao.observeItems(id)) { receipt, items ->
            receipt?.toDomain(items.map { it.toDomain() })
        }

    override suspend fun upsert(receipt: Receipt) {
        dao.upsert(receipt.toEntity())
        dao.upsertItems(receipt.items.map { it.toEntity(receipt.id) })
    }

    override suspend fun delete(id: String) = dao.delete(id)
    override suspend fun search(query: String): List<Receipt> = dao.search(query).map { it.toDomain(emptyList()) }
}
