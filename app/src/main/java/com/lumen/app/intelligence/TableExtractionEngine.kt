package com.lumen.app.intelligence

import com.lumen.app.domain.model.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Real, heuristic Table Intelligence Engine: clusters OCR line blocks into rows (by Y overlap)
 * and columns (by X-position gaps), infers a header row and per-column data types, then produces
 * an editable [LumenDataTable]. This is a classic OCR-table-reconstruction heuristic (row/column
 * clustering on bounding boxes) rather than a trained table-detection model — it works well for
 * grid-like printed tables and is fully editable afterwards in the Table Detail screen, so
 * mis-clustering is always user-correctable rather than a silent failure.
 */
@Singleton
class TableExtractionEngine @Inject constructor() {

    fun extract(sourceDocumentId: String, title: String, blocks: List<OcrTextBlock>): LumenDataTable? {
        if (blocks.size < 4) return null // not enough content to look like a table

        val rows = clusterIntoRows(blocks)
        if (rows.size < 2) return null

        val columnAnchors = deriveColumnAnchors(rows)
        if (columnAnchors.size < 2) return null

        val columns = columnAnchors.mapIndexed { index, _ ->
            TableColumn(
                id = UUID.randomUUID().toString(),
                name = "Column ${index + 1}",
                dataType = ColumnDataType.TEXT,
                orderIndex = index,
            )
        }

        val tableRows = rows.mapIndexed { rowIndex, rowBlocks ->
            val cells = columns.mapIndexed { colIndex, column ->
                val anchor = columnAnchors[colIndex]
                val cellBlock = rowBlocks.minByOrNull { abs(centerX(it) - anchor) }
                TableCell(
                    columnId = column.id,
                    value = cellBlock?.text.orEmpty(),
                    confidence = cellBlock?.confidence ?: 0f,
                )
            }
            TableRow(id = UUID.randomUUID().toString(), orderIndex = rowIndex, cells = cells)
        }

        // Promote first row to header names if it looks textual (non-numeric-heavy).
        val headerRow = tableRows.firstOrNull()
        val finalColumns = if (headerRow != null && looksLikeHeader(headerRow)) {
            columns.mapIndexed { i, c -> c.copy(name = headerRow.cells.getOrNull(i)?.value?.ifBlank { c.name } ?: c.name) }
        } else columns

        val dataRows = if (headerRow != null && looksLikeHeader(headerRow)) tableRows.drop(1) else tableRows
        val typedColumns = finalColumns.mapIndexed { i, c -> c.copy(dataType = inferColumnType(dataRows, c.id, i)) }

        return LumenDataTable(
            id = UUID.randomUUID().toString(),
            sourceDocumentId = sourceDocumentId,
            title = title,
            columns = typedColumns,
            rows = dataRows.mapIndexed { idx, r -> r.copy(orderIndex = idx) },
            createdAtEpochMs = System.currentTimeMillis(),
        )
    }

    private fun centerX(block: OcrTextBlock) = (block.boundingBox.left + block.boundingBox.right) / 2f
    private fun centerY(block: OcrTextBlock) = (block.boundingBox.top + block.boundingBox.bottom) / 2f

    private fun clusterIntoRows(blocks: List<OcrTextBlock>): List<List<OcrTextBlock>> {
        val sorted = blocks.sortedBy { centerY(it) }
        val rows = mutableListOf<MutableList<OcrTextBlock>>()
        val rowHeightThreshold = (blocks.map { it.boundingBox.bottom - it.boundingBox.top }.average()).toFloat() * 0.7f
        for (block in sorted) {
            val row = rows.lastOrNull { row -> abs(centerY(row.first()) - centerY(block)) < rowHeightThreshold }
            if (row != null) row.add(block) else rows.add(mutableListOf(block))
        }
        return rows.map { row -> row.sortedBy { centerX(it) } }
    }

    private fun deriveColumnAnchors(rows: List<List<OcrTextBlock>>): List<Float> {
        val bestRow = rows.maxByOrNull { it.size } ?: return emptyList()
        return bestRow.map { centerX(it) }.sorted()
    }

    private fun looksLikeHeader(row: TableRow): Boolean {
        val numericCells = row.cells.count { it.value.any { c -> c.isDigit() } }
        return numericCells <= row.cells.size / 3
    }

    private fun inferColumnType(rows: List<TableRow>, columnId: String, columnIndex: Int): ColumnDataType {
        val values = rows.mapNotNull { it.cells.getOrNull(columnIndex)?.value }.filter { it.isNotBlank() }
        if (values.isEmpty()) return ColumnDataType.TEXT
        val numericRegex = Regex("^-?\\d+([.,]\\d+)?$")
        val currencyRegex = Regex("^[\\$€£₺﷼]?\\s?-?\\d+([.,]\\d+)?\\s?[\\$€£₺﷼]?$")
        val dateRegex = Regex("^\\d{1,4}[/-]\\d{1,2}[/-]\\d{1,4}$")
        val boolRegex = Regex("^(true|false|yes|no|بله|خیر|是|否)$", RegexOption.IGNORE_CASE)
        return when {
            values.all { dateRegex.matches(it.trim()) } -> ColumnDataType.DATE
            values.all { currencyRegex.matches(it.trim()) } && values.any { it.any { c -> c in "$€£₺﷼" } } -> ColumnDataType.CURRENCY
            values.all { numericRegex.matches(it.trim()) } -> ColumnDataType.NUMBER
            values.all { boolRegex.matches(it.trim()) } -> ColumnDataType.BOOLEAN
            else -> ColumnDataType.TEXT
        }
    }
}
