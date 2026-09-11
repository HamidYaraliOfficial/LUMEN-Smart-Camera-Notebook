package com.lumen.app.domain.model

/** Normalised bounding box (0..1 range relative to the source image) so it survives resizing. */
data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/** A single recognised word/line/block, with provenance back to the source image. */
data class OcrTextBlock(
    val id: String,
    val text: String,
    val boundingBox: NormalizedRect,
    val confidence: Float,
    val languageHint: String?,
    val lineIndex: Int,
    val blockIndex: Int,
)

/** Full OCR result for one captured page/image. */
data class OcrResult(
    val id: String,
    val sourceImageId: String,
    val fullText: String,
    val blocks: List<OcrTextBlock>,
    val detectedLanguage: String?,
    val averageConfidence: Float,
    val createdAtEpochMs: Long,
) {
    val confidenceLevel: ConfidenceLevel get() = ConfidenceLevel.from(averageConfidence)
    val lowConfidenceBlocks: List<OcrTextBlock> get() = blocks.filter { it.confidence < 0.6f }
}

/** A field extracted from a receipt/form/business card, always traceable to its source block. */
data class ExtractedField(
    val key: String,
    val value: String,
    val confidence: Float,
    val sourceBlockId: String?,
) {
    val confidenceLevel: ConfidenceLevel get() = ConfidenceLevel.from(confidence)
}
