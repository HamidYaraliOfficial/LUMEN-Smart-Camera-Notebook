package com.lumen.app.data.local.dao

import androidx.room.*
import com.lumen.app.data.local.entity.NotebookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {
    @Query("SELECT * FROM notebooks WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAtEpochMs DESC")
    fun observeActive(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE isArchived = 1 ORDER BY updatedAtEpochMs DESC")
    fun observeArchived(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE id = :id")
    suspend fun getById(id: String): NotebookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(notebook: NotebookEntity)

    @Update
    suspend fun update(notebook: NotebookEntity)

    @Query("DELETE FROM notebooks WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE notebooks SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("UPDATE notebooks SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("UPDATE notebooks SET isLocked = :locked WHERE id = :id")
    suspend fun setLocked(id: String, locked: Boolean)
}
