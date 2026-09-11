package com.lumen.app.data.local.dao

import androidx.room.*
import com.lumen.app.data.local.entity.NoteEntity
import com.lumen.app.data.local.entity.NoteVersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE notebookId = :notebookId AND isArchived = 0 ORDER BY isPinned DESC, updatedAtEpochMs DESC")
    fun observeByNotebook(notebookId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isFavorite = 1 AND isArchived = 0 ORDER BY updatedAtEpochMs DESC")
    fun observeFavorites(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAtEpochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id")
    fun observeById(id: String): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)

    @Query(
        """
        SELECT notes.* FROM notes
        JOIN notes_fts ON notes.rowid = notes_fts.rowid
        WHERE notes_fts MATCH :query
        ORDER BY updatedAtEpochMs DESC
        """
    )
    suspend fun searchFts(query: String): List<NoteEntity>

    @Insert
    suspend fun insertVersion(version: NoteVersionEntity)

    @Query("SELECT * FROM note_versions WHERE noteId = :noteId ORDER BY versionNumber DESC")
    fun observeVersions(noteId: String): Flow<List<NoteVersionEntity>>
}
