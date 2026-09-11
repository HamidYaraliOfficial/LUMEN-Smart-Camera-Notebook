package com.lumen.app.domain.model

/** The camera / capture mode the user selected (or LUMEN auto-suggested). */
enum class ScanMode {
    DOCUMENT, BOARD, BOOK, RECEIPT, TABLE, HANDWRITING, BUSINESS_CARD, QR_BARCODE, FREE_SCAN
}

/** What kind of structured item a scan was turned into. */
enum class ContentType {
    NOTE, DOCUMENT, TABLE, TASK, REMINDER, RECEIPT, FLASHCARD, SUMMARY, QUOTE, CONTACT, EVENT, STRUCTURED_DATA
}

/** Confidence bucket derived from a raw 0..1 OCR/extraction confidence score. */
enum class ConfidenceLevel { HIGH, MEDIUM, LOW;
    companion object {
        fun from(score: Float): ConfidenceLevel = when {
            score >= 0.85f -> HIGH
            score >= 0.6f -> MEDIUM
            else -> LOW
        }
    }
}

/** Lifecycle status of an asynchronous processing job (batch scan, backup, export...). */
enum class JobStatus { QUEUED, PROCESSING, REVIEW, COMPLETED, FAILED, CANCELLED }

/** Stage of the Capture -> ... -> Save processing pipeline, used for per-stage progress UI. */
enum class PipelineStage {
    CAPTURE, PREPROCESS, DETECT, OCR, STRUCTURE, CLEAN, ANALYZE, SUMMARIZE, INDEX, SAVE
}

enum class ExportFormat { PDF, TXT, MARKDOWN, CSV, JSON, IMAGE, XLSX_COMPATIBLE }

enum class TranslationProviderType { ON_DEVICE, CLOUD }
