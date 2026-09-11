package com.lumen.app.data.local.dao

import androidx.room.*
import com.lumen.app.data.local.entity.DataTableEntity
import com.lumen.app.data.local.entity.TableCellEntity
import com.lumen.app.data.local.entity.TableColumnEntity
import com.lumen.app.data.local.entity.TableRowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTable(table: DataTableEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertColumns(columns: List<TableColumnEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRows(rows: List<TableRowEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCells(cells: List<TableCellEntity>)

    @Query("SELECT * FROM data_tables WHERE sourceDocumentId = :documentId")
    fun observeByDocument(documentId: String): Flow<List<DataTableEntity>>

    @Query("SELECT * FROM table_columns WHERE tableId = :tableId ORDER BY orderIndex")
    fun observeColumns(tableId: String): Flow<List<TableColumnEntity>>

    @Query("SELECT * FROM table_rows WHERE tableId = :tableId ORDER BY orderIndex")
    fun observeRows(tableId: String): Flow<List<TableRowEntity>>

    @Query("SELECT * FROM table_cells WHERE rowId = :rowId")
    suspend fun getCellsForRow(rowId: String): List<TableCellEntity>

    @Query("DELETE FROM table_rows WHERE id = :rowId")
    suspend fun deleteRow(rowId: String)

    @Query("DELETE FROM table_columns WHERE id = :columnId")
    suspend fun deleteColumn(columnId: String)

    @Query("UPDATE table_cells SET value = :value WHERE rowId = :rowId AND columnId = :columnId")
    suspend fun updateCell(rowId: String, columnId: String, value: String)
}
