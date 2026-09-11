package com.lumen.app.intelligence

import com.lumen.app.domain.model.ExtractedField
import com.lumen.app.domain.model.OcrResult
import com.lumen.app.domain.model.OcrTextBlock
import com.lumen.app.domain.model.Receipt
import com.lumen.app.domain.model.ReceiptItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real, regex/heuristic Receipt & Invoice parser. It is intentionally a transparent, offline,
 * rule-based extractor (not a black-box ML model) so every field stays traceable to the OCR line
 * it came from via [ExtractedField.sourceBlockId] — exactly what the Provenance Layer needs.
 */
@Singleton
class ReceiptParsingEngine @Inject constructor() {

    private val totalKeywords = listOf("total", "grand total", "amount due", "جمع کل", "مبلغ قابل پرداخت", "总计", "合计")
    private val taxKeywords = listOf("tax", "vat", "gst", "مالیات", "税")
    private val dateRegex = Regex("(\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})|(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})")
    private val moneyRegex = Regex("([\\$€£₺﷼¥])?\\s?(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)\\s?([\\$€£₺﷼¥])?")
    private val currencySymbols = setOf('$', '€', '£', '₺', '﷼', '¥')

    fun parse(notebookId: String, sourceDocumentId: String, ocr: OcrResult): Receipt {
        val lines = ocr.blocks

        val totalField = findAmountField(lines, totalKeywords)
        val taxField = findAmountField(lines, taxKeywords)
        val dateField = findDateField(lines)
        val merchantField = guessMerchant(lines)
        val currencyField = guessCurrency(lines, totalField)
        val items = extractItems(lines, excluding = listOfNotNull(totalField, taxField, dateField, merchantField).mapNotNull { it.sourceBlockId })

        return Receipt(
            id = UUID.randomUUID().toString(),
            notebookId = notebookId,
            sourceDocumentId = sourceDocumentId,
            merchant = merchantField,
            date = dateField,
            total = totalField,
            tax = taxField,
            currency = currencyField,
            paymentMethod = guessPaymentMethod(lines),
            items = items,
            category = null,
            tags = emptyList(),
            createdAtEpochMs = System.currentTimeMillis(),
        )
    }

    private fun findAmountField(lines: List<OcrTextBlock>, keywords: List<String>): ExtractedField? {
        val candidate = lines.firstOrNull { line ->
            keywords.any { kw -> line.text.contains(kw, ignoreCase = true) }
        } ?: return null
        val match = moneyRegex.find(candidate.text) ?: run {
            // amount might be on the next line for narrow receipts; caller can re-scan if needed
            null
        }
        val value = match?.value?.trim() ?: candidate.text.trim()
        return ExtractedField(key = keywords.first(), value = value, confidence = candidate.confidence, sourceBlockId = candidate.id)
    }

    private fun findDateField(lines: List<OcrTextBlock>): ExtractedField? {
        for (line in lines) {
            val match = dateRegex.find(line.text)
            if (match != null) {
                return ExtractedField("date", match.value, line.confidence, line.id)
            }
        }
        return null
    }

    private fun guessMerchant(lines: List<OcrTextBlock>): ExtractedField? {
        // Heuristic: the first non-empty, mostly-alphabetic line near the top of the receipt.
        val topLines = lines.sortedBy { it.blockIndex * 1000 + it.lineIndex }.take(5)
        val candidate = topLines.firstOrNull { it.text.trim().length in 3..40 && it.text.any { c -> c.isLetter() } }
        return candidate?.let { ExtractedField("merchant", it.text.trim(), it.confidence, it.id) }
    }

    private fun guessCurrency(lines: List<OcrTextBlock>, totalField: ExtractedField?): ExtractedField? {
        val source = totalField?.value ?: lines.joinToString(" ") { it.text }
        val symbol = source.firstOrNull { it in currencySymbols }
        return symbol?.let { ExtractedField("currency", it.toString(), totalField?.confidence ?: 0.5f, totalField?.sourceBlockId) }
    }

    private fun guessPaymentMethod(lines: List<OcrTextBlock>): ExtractedField? {
        val keywords = mapOf(
            "cash" to listOf("cash", "نقدی", "现金"),
            "card" to listOf("card", "visa", "mastercard", "کارت", "刷卡"),
        )
        for ((method, kws) in keywords) {
            val line = lines.firstOrNull { l -> kws.any { l.text.contains(it, ignoreCase = true) } }
            if (line != null) return ExtractedField("paymentMethod", method, line.confidence, line.id)
        }
        return null
    }

    private fun extractItems(lines: List<OcrTextBlock>, excluding: List<String>): List<ReceiptItem> {
        val itemLineRegex = Regex("^(.{2,40}?)\\s+(\\d+(?:[.,]\\d+)?)?\\s*[xX×]?\\s*(\\d+(?:[.,]\\d{2})?)$")
        return lines.filter { it.id !in excluding }.mapNotNull { line ->
            val match = itemLineRegex.find(line.text.trim()) ?: return@mapNotNull null
            val name = match.groupValues[1].trim()
            val qty = match.groupValues[2].toDoubleOrNull()
            val price = match.groupValues[3].replace(",", ".").toDoubleOrNull()
            if (name.isBlank() || price == null) return@mapNotNull null
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = name,
                quantity = qty,
                unitPrice = if (qty != null && qty > 0) price / qty else price,
                lineTotal = price,
                confidence = line.confidence,
            )
        }
    }
}
