package com.lumen.app.domain.model

data class ReceiptItem(
    val id: String,
    val name: String,
    val quantity: Double?,
    val unitPrice: Double?,
    val lineTotal: Double?,
    val confidence: Float,
)

data class Receipt(
    val id: String,
    val notebookId: String,
    val sourceDocumentId: String,
    val merchant: ExtractedField?,
    val date: ExtractedField?,
    val total: ExtractedField?,
    val tax: ExtractedField?,
    val currency: ExtractedField?,
    val paymentMethod: ExtractedField?,
    val items: List<ReceiptItem>,
    val category: String?,
    val tags: List<String>,
    val createdAtEpochMs: Long,
) {
    val overallConfidence: Float
        get() {
            val vals = listOfNotNull(merchant?.confidence, date?.confidence, total?.confidence, tax?.confidence)
            return if (vals.isEmpty()) 0f else vals.average().toFloat()
        }
}
