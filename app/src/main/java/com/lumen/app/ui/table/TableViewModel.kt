package com.lumen.app.ui.table

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.app.domain.model.TableColumn
import com.lumen.app.domain.model.TableRow
import com.lumen.app.domain.repository.TableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TableUiState(val columns: List<TableColumn> = emptyList(), val rows: List<TableRow> = emptyList())

@HiltViewModel
class TableViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TableRepository,
) : ViewModel() {
    private val tableId: String = checkNotNull(savedStateHandle["tableId"])

    val uiState: StateFlow<TableUiState> = combine(
        repository.observeColumns(tableId),
        repository.observeRows(tableId),
    ) { columns, rows -> TableUiState(columns, rows) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TableUiState())

    fun updateCell(rowId: String, columnId: String, value: String) {
        viewModelScope.launch { repository.updateCell(rowId, columnId, value) }
    }

    fun deleteRow(rowId: String) {
        viewModelScope.launch { repository.deleteRow(rowId) }
    }

    fun deleteColumn(columnId: String) {
        viewModelScope.launch { repository.deleteColumn(columnId) }
    }
}
