package com.lumen.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "receipts")
@TypeConverters(Converters::class)
data class ReceiptEntity(
    @PrimaryKey val id: String,
    val notebookId: String,
    val sourceDocumentId: String,
    val merchantValue: String?,
    val merchantConfidence: Float?,
    val dateValue: String?,
    val dateConfidence: Float?,
    val totalValue: String?,
    val totalConfidence: Float?,
    val taxValue: String?,
    val taxConfidence: Float?,
    val currencyValue: String?,
    val paymentMethodValue: String?,
    val category: String?,
    val tags: List<String>,
    val createdAtEpochMs: Long,
)

@Entity(tableName = "receipt_items")
data class ReceiptItemEntity(
    @PrimaryKey val id: String,
    val receiptId: String,
    val name: String,
    val quantity: Double?,
    val unitPrice: Double?,
    val lineTotal: Double?,
    val confidence: Float,
)
