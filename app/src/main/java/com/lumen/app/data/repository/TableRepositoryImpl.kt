package com.lumen.app.data.repository

import com.lumen.app.data.local.dao.TableDao
import com.lumen.app.data.local.entity.DataTableEntity
import com.lumen.app.data.local.entity.TableCellEntity
import com.lumen.app.data.local.entity.TableColumnEntity
import com.lumen.app.data.local.entity.TableRowEntity
import com.lumen.app.domain.model.LumenDataTable
import com.lumen.app.domain.model.TableColumn
import com.lumen.app.domain.model.TableRow
import com.lumen.app.domain.repository.TableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TableRepositoryImpl @Inject constructor(
    private val dao: TableDao
) : TableRepository {

    override suspend fun upsert(table: LumenDataTable) {
        dao.upsertTable(DataTableEntity(table.id, table.sourceDocumentId, table.title, table.createdAtEpochMs))
        dao.upsertColumns(table.columns.map { TableColumnEntity(it.id, table.id, it.name, it.dataType.name, it.orderIndex) })
        dao.upsertRows(table.rows.map { TableRowEntity(it.id, table.id, it.orderIndex) })
        val cells = table.rows.flatMap { row -> row.cells.map { cell -> TableCellEntity(rowId = row.id, columnId = cell.columnId, value = cell.value, confidence = cell.confidence) } }
        dao.upsertCells(cells)
    }

    override fun observeByDocument(documentId: String): Flow<List<LumenDataTable>> =
        dao.observeByDocument(documentId).map { list ->
            list.map { entity -> LumenDataTable(entity.id, entity.sourceDocumentId, entity.title, emptyList(), emptyList(), entity.createdAtEpochMs) }
        }

    override fun observeColumns(tableId: String): Flow<List<TableColumn>> =
        dao.observeColumns(tableId).map { list ->
            list.map { TableColumn(it.id, it.name, runCatching { com.lumen.app.domain.model.ColumnDataType.valueOf(it.dataType) }.getOrDefault(com.lumen.app.domain.model.ColumnDataType.TEXT), it.orderIndex) }
        }

    override fun observeRows(tableId: String): Flow<List<TableRow>> =
        dao.observeRows(tableId).map { rows ->
            rows.map { row ->
                val cells = dao.getCellsForRow(row.id).map { com.lumen.app.domain.model.TableCell(it.columnId, it.value, it.confidence) }
                TableRow(row.id, row.orderIndex, cells)
            }
        }

    override suspend fun updateCell(rowId: String, columnId: String, value: String) = dao.updateCell(rowId, columnId, value)
    override suspend fun deleteRow(rowId: String) = dao.deleteRow(rowId)
    override suspend fun deleteColumn(columnId: String) = dao.deleteColumn(columnId)
}
