package com.lumen.app.data.local.dao

import androidx.room.*
import com.lumen.app.data.local.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlashcard(card: FlashcardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlashcards(cards: List<FlashcardEntity>)

    @Query("SELECT * FROM flashcards WHERE notebookId = :notebookId ORDER BY createdAtEpochMs DESC")
    fun observeByNotebook(notebookId: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<FlashcardEntity>>

    @Query("UPDATE flashcards SET timesReviewed = timesReviewed + 1, lastReviewedAtEpochMs = :now WHERE id = :id")
    suspend fun markReviewed(id: String, now: Long)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun delete(id: String)
}
