package com.lumen.app.domain.model

data class ScanJob(
    val id: String,
    val status: JobStatus,
    val stage: PipelineStage,
    val progressPercent: Int,
    val totalPages: Int,
    val processedPages: Int,
    val errorMessage: String? = null,
    val createdAtEpochMs: Long,
)

data class DuplicateCandidate(
    val documentIdA: String,
    val documentIdB: String,
    val similarityPercent: Int, // 0..100, perceptual-hash based
)

data class BookmarkedCode(
    val id: String,
    val rawValue: String,
    val format: String, // QR_CODE, EAN_13, CODE_128, URL, ...
    val isUrl: Boolean,
    val savedAtEpochMs: Long,
)

data class SmartClipboardEntry(
    val id: String,
    val text: String,
    val sourceDocumentId: String?,
    val copiedAtEpochMs: Long,
)

data class BackupRecord(
    val id: String,
    val fileUri: String,
    val sizeBytes: Long,
    val isEncrypted: Boolean,
    val createdAtEpochMs: Long,
)

/** Which categories of data the user allows to be sent to an optional cloud AI/translation provider. */
data class CloudConsent(
    val allowImages: Boolean = false,
    val allowOcrText: Boolean = false,
    val allowReceipts: Boolean = false,
    val allowHandwriting: Boolean = false,
)

data class StorageBreakdown(
    val originalImagesBytes: Long,
    val processedImagesBytes: Long,
    val ocrDataBytes: Long,
    val pdfsBytes: Long,
    val thumbnailsBytes: Long,
    val aiDataBytes: Long,
    val cacheBytes: Long,
) {
    val totalBytes: Long get() = originalImagesBytes + processedImagesBytes + ocrDataBytes + pdfsBytes + thumbnailsBytes + aiDataBytes + cacheBytes
}
