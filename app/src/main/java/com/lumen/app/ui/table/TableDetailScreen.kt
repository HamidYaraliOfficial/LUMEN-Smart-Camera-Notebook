package com.lumen.app.ui.table

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumen.app.ui.components.LumenTopBar

@Composable
fun TableDetailScreen(
    onBack: () -> Unit,
    viewModel: TableViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(topBar = { LumenTopBar(title = "Table", onBack = onBack) }) { padding ->
        Column(modifier = Modifier.padding(padding).horizontalScroll(rememberScrollState())) {
            Row(modifier = Modifier.padding(8.dp)) {
                state.columns.forEach { col ->
                    Box(modifier = Modifier.width(140.dp).padding(4.dp)) {
                        Text(col.name, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.width(40.dp))
            }
            LazyColumn {
                items(state.rows, key = { it.id }) { row ->
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                        state.columns.forEach { col ->
                            val cellValue = row.cells.firstOrNull { it.columnId == col.id }?.value.orEmpty()
                            var text by remember(row.id, col.id) { mutableStateOf(cellValue) }
                            OutlinedTextField(
                                value = text,
                                onValueChange = {
                                    text = it
                                    viewModel.updateCell(row.id, col.id, it)
                                },
                                modifier = Modifier.width(140.dp).padding(4.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall,
                            )
                        }
                        IconButton(onClick = { viewModel.deleteRow(row.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete row")
                        }
                    }
                }
            }
        }
    }
}
