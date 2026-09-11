package com.lumen.app.domain.model

enum class ColumnDataType { TEXT, NUMBER, DATE, CURRENCY, BOOLEAN }

data class TableColumn(
    val id: String,
    val name: String,
    val dataType: ColumnDataType,
    val orderIndex: Int,
)

data class TableCell(
    val columnId: String,
    val value: String,
    val confidence: Float = 1f,
)

data class TableRow(
    val id: String,
    val orderIndex: Int,
    val cells: List<TableCell>,
)

data class LumenDataTable(
    val id: String,
    val sourceDocumentId: String,
    val title: String,
    val columns: List<TableColumn>,
    val rows: List<TableRow>,
    val createdAtEpochMs: Long,
)
