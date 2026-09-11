package com.lumen.app.data.local.dao

import androidx.room.*
import com.lumen.app.data.local.entity.ReceiptEntity
import com.lumen.app.data.local.entity.ReceiptItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts WHERE notebookId = :notebookId ORDER BY createdAtEpochMs DESC")
    fun observeByNotebook(notebookId: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts ORDER BY createdAtEpochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    fun observeById(id: String): Flow<ReceiptEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(receipt: ReceiptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<ReceiptItemEntity>)

    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    fun observeItems(receiptId: String): Flow<List<ReceiptItemEntity>>

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM receipts WHERE merchantValue LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<ReceiptEntity>
}
