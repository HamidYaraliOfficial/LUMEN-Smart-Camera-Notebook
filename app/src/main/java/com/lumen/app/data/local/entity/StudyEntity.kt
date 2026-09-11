package com.lumen.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "flashcards")
@TypeConverters(Converters::class)
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val notebookId: String,
    val sourceDocumentId: String?,
    val question: String,
    val answer: String,
    val tags: List<String>,
    val timesReviewed: Int,
    val lastReviewedAtEpochMs: Long?,
    val createdAtEpochMs: Long,
)
