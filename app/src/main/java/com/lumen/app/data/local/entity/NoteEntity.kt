package com.lumen.app.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class NoteEntity(
    @PrimaryKey val id: String,
    val notebookId: String,
    val title: String,
    val bodyMarkdown: String,
    val ocrResultId: String?,
    val sourceDocumentId: String?,
    val tags: List<String>,
    val isFavorite: Boolean,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val version: Int,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

@Entity(tableName = "note_versions")
data class NoteVersionEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val versionNumber: Int,
    val bodyMarkdownSnapshot: String,
    val editedAtEpochMs: Long,
)

/** Full-text-search shadow table over note title + body for the local Search Engine. */
@Fts4(contentEntity = NoteEntity::class)
@Entity(tableName = "notes_fts")
data class NoteFtsEntity(
    val title: String,
    val bodyMarkdown: String,
)
