package com.lumen.app.domain.model

data class DocumentPage(
    val id: String,
    val pageIndex: Int,
    val originalImageUri: String,
    val processedImageUri: String?,
    val ocrResultId: String?,
    val rotationDegrees: Int = 0,
)

data class LumenDocument(
    val id: String,
    val notebookId: String,
    val title: String,
    val scanMode: ScanMode,
    val pages: List<DocumentPage>,
    val summaryShort: String?,
    val summaryMedium: String?,
    val summaryDeep: String?,
    val keywords: List<String>,
    val tags: List<String>,
    val pdfUri: String?,
    val isLocked: Boolean = false,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
