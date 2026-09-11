package com.lumen.app.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "documents")
@TypeConverters(Converters::class)
data class DocumentEntity(
    @PrimaryKey val id: String,
    val notebookId: String,
    val title: String,
    val scanMode: String,
    val summaryShort: String?,
    val summaryMedium: String?,
    val summaryDeep: String?,
    val keywords: List<String>,
    val tags: List<String>,
    val pdfUri: String?,
    val isLocked: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

@Fts4(contentEntity = DocumentEntity::class)
@Entity(tableName = "documents_fts")
data class DocumentFtsEntity(
    val title: String,
    val summaryMedium: String?,
)

@Entity(tableName = "document_pages")
data class DocumentPageEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val pageIndex: Int,
    val originalImageUri: String,
    val processedImageUri: String?,
    val ocrResultId: String?,
    val rotationDegrees: Int,
)
