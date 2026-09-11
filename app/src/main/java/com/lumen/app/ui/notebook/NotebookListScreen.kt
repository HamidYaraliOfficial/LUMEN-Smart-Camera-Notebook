package com.lumen.app.ui.notebook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumen.app.ui.components.EmptyState
import com.lumen.app.ui.components.LumenCard
import com.lumen.app.ui.components.LumenTopBar

@Composable
fun NotebookListScreen(
    onOpenNotebook: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: NotebookViewModel = hiltViewModel(),
) {
    val notebooks by viewModel.notebooks.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.seedDefaultsIfEmpty() }

    Scaffold(
        topBar = { LumenTopBar(title = "Notebooks", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) { Icon(Icons.Filled.Add, null) }
        }
    ) { padding ->
        if (notebooks.isEmpty()) {
            EmptyState(
                title = "No notebooks yet",
                subtitle = "Create one to start organising your scans.",
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(padding).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(notebooks, key = { it.id }) { notebook ->
                    LumenCard(modifier = Modifier.clickable { onOpenNotebook(notebook.id) }) {
                        Text(notebook.name, fontWeight = FontWeight.SemiBold)
                        Text("${notebook.itemCount} items", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (showCreateDialog) {
            var name by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("New notebook") },
                text = {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (name.isNotBlank()) viewModel.createNotebook(name, "#0078D4")
                        showCreateDialog = false
                    }) { Text("Create") }
                },
                dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
            )
        }
    }
}
