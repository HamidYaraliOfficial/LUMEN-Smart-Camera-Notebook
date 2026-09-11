package com.lumen.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_codes")
data class BookmarkedCodeEntity(
    @PrimaryKey val id: String,
    val rawValue: String,
    val format: String,
    val isUrl: Boolean,
    val savedAtEpochMs: Long,
)

@Entity(tableName = "smart_clipboard")
data class SmartClipboardEntity(
    @PrimaryKey val id: String,
    val text: String,
    val sourceDocumentId: String?,
    val copiedAtEpochMs: Long,
)

@Entity(tableName = "scan_jobs")
data class ScanJobEntity(
    @PrimaryKey val id: String,
    val status: String,
    val stage: String,
    val progressPercent: Int,
    val totalPages: Int,
    val processedPages: Int,
    val errorMessage: String?,
    val createdAtEpochMs: Long,
)

@Entity(tableName = "backup_records")
data class BackupRecordEntity(
    @PrimaryKey val id: String,
    val fileUri: String,
    val sizeBytes: Long,
    val isEncrypted: Boolean,
    val createdAtEpochMs: Long,
)
