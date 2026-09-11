package com.lumen.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_tables")
data class DataTableEntity(
    @PrimaryKey val id: String,
    val sourceDocumentId: String,
    val title: String,
    val createdAtEpochMs: Long,
)

@Entity(tableName = "table_columns")
data class TableColumnEntity(
    @PrimaryKey val id: String,
    val tableId: String,
    val name: String,
    val dataType: String,
    val orderIndex: Int,
)

@Entity(tableName = "table_rows")
data class TableRowEntity(
    @PrimaryKey val id: String,
    val tableId: String,
    val orderIndex: Int,
)

@Entity(tableName = "table_cells")
data class TableCellEntity(
    @PrimaryKey(autoGenerate = true) val autoId: Long = 0,
    val rowId: String,
    val columnId: String,
    val value: String,
    val confidence: Float,
)
