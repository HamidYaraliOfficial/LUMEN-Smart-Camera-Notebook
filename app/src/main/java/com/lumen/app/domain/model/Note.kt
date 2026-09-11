package com.lumen.app.domain.model

data class NoteAttachment(
    val id: String,
    val type: String, // image | pdf | audio | link
    val uri: String,
    val label: String? = null,
)

data class Note(
    val id: String,
    val notebookId: String,
    val title: String,
    val bodyMarkdown: String,
    val ocrResultId: String?,
    val sourceDocumentId: String?,
    val tags: List<String>,
    val attachments: List<NoteAttachment>,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val version: Int = 1,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

data class NoteVersion(
    val id: String,
    val noteId: String,
    val versionNumber: Int,
    val bodyMarkdownSnapshot: String,
    val editedAtEpochMs: Long,
)
