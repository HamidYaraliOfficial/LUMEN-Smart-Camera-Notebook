package com.lumen.app.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "ocr_results")
@TypeConverters(Converters::class)
data class OcrResultEntity(
    @PrimaryKey val id: String,
    val sourceImageId: String,
    val fullText: String,
    val detectedLanguage: String?,
    val averageConfidence: Float,
    val createdAtEpochMs: Long,
)

@Fts4(contentEntity = OcrResultEntity::class)
@Entity(tableName = "ocr_results_fts")
data class OcrResultFtsEntity(
    val fullText: String,
)

@Entity(tableName = "ocr_blocks")
data class OcrTextBlockEntity(
    @PrimaryKey val id: String,
    val ocrResultId: String,
    val text: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val confidence: Float,
    val languageHint: String?,
    val lineIndex: Int,
    val blockIndex: Int,
)
