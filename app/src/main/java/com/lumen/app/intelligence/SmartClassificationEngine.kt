package com.lumen.app.intelligence

import com.lumen.app.domain.model.OcrResult
import com.lumen.app.domain.model.ScanMode

/** Real heuristic classifier: suggests a [ScanMode] from OCR text shape/content. Always
 * user-correctable — this only pre-selects a suggestion, it never locks the classification. */
class SmartClassificationEngine {

    private val moneyRegex = Regex("[\\$€£₺﷼¥]\\s?\\d")
    private val emailRegex = Regex("[\\w.+-]+@[\\w-]+\\.[\\w.-]+")
    private val phoneRegex = Regex("\\+?\\d[\\d\\s().-]{6,}\\d")
    private val tableRowRegex = Regex("(\\S+\\s+){2,}\\S+\\s+\\d")

    fun classify(ocr: OcrResult): ScanMode {
        val text = ocr.fullText
        val lines = text.lines().filter { it.isNotBlank() }
        val moneyHits = moneyRegex.findAll(text).count()
        val hasEmail = emailRegex.containsMatchIn(text)
        val hasPhone = phoneRegex.containsMatchIn(text)
        val tableLikeLines = lines.count { tableRowRegex.containsMatchIn(it) }

        return when {
            hasEmail && hasPhone && lines.size <= 12 -> ScanMode.BUSINESS_CARD
            moneyHits >= 3 && lines.any { it.contains("total", true) || it.contains("جمع", true) || it.contains("总", true) } -> ScanMode.RECEIPT
            tableLikeLines >= 3 -> ScanMode.TABLE
            lines.size > 25 && ocr.averageConfidence > 0.6f -> ScanMode.BOOK
            ocr.averageConfidence < 0.45f -> ScanMode.HANDWRITING
            lines.size in 1..6 -> ScanMode.BOARD
            else -> ScanMode.DOCUMENT
        }
    }
}
