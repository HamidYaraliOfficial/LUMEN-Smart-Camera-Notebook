package com.lumen.app.intelligence

import com.lumen.app.domain.model.ExtractedField
import com.lumen.app.domain.model.OcrResult

/**
 * Real Key/Value form-field extractor: looks for "Label: Value" / "Label - Value" patterns
 * (common on printed forms in Latin, Persian and Chinese punctuation styles) plus a small set of
 * well-known field labels, and returns them as traceable [ExtractedField]s.
 */
class FormFieldExtractionEngine {

    private val separators = Regex("[:：ـ-]\\s*")
    private val knownLabels = listOf(
        "name", "date", "address", "phone", "amount", "number", "id",
        "نام", "تاریخ", "آدرس", "شماره", "مبلغ",
        "姓名", "日期", "地址", "电话", "金额",
    )

    fun extract(ocr: OcrResult): List<ExtractedField> {
        val results = mutableListOf<ExtractedField>()
        for (block in ocr.blocks) {
            val parts = block.text.split(separators, limit = 2)
            if (parts.size == 2) {
                val label = parts[0].trim()
                val value = parts[1].trim()
                if (label.isNotBlank() && value.isNotBlank() &&
                    (knownLabels.any { label.contains(it, ignoreCase = true) } || label.length <= 20)
                ) {
                    results += ExtractedField(key = label, value = value, confidence = block.confidence, sourceBlockId = block.id)
                }
            }
        }
        return results
    }
}
