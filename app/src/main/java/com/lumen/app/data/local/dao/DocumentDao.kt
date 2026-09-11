package com.lumen.app.data.local.dao

import androidx.room.*
import com.lumen.app.data.local.entity.DocumentEntity
import com.lumen.app.data.local.entity.DocumentPageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE notebookId = :notebookId ORDER BY updatedAtEpochMs DESC")
    fun observeByNotebook(notebookId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents ORDER BY updatedAtEpochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :id")
    fun observeById(id: String): Flow<DocumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun delete(id: String)

    @Query(
        """
        SELECT documents.* FROM documents
        JOIN documents_fts ON documents.rowid = documents_fts.rowid
        WHERE documents_fts MATCH :query
        ORDER BY updatedAtEpochMs DESC
        """
    )
    suspend fun searchFts(query: String): List<DocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPage(page: DocumentPageEntity)

    @Query("SELECT * FROM document_pages WHERE documentId = :documentId ORDER BY pageIndex ASC")
    fun observePages(documentId: String): Flow<List<DocumentPageEntity>>

    @Query("DELETE FROM document_pages WHERE id = :pageId")
    suspend fun deletePage(pageId: String)

    @Query("UPDATE document_pages SET pageIndex = :newIndex WHERE id = :pageId")
    suspend fun reorderPage(pageId: String, newIndex: Int)
}
