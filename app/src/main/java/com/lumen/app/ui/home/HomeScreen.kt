package com.lumen.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumen.app.ui.components.LumenCard

@Composable
fun HomeScreen(
    onQuickScan: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDocument: (String) -> Unit,
    onOpenNote: (String) -> Unit,
    onOpenReceipt: (String) -> Unit,
    onOpenSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LUMEN", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenSearch) { Icon(Icons.Filled.Search, contentDescription = "Search") }
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onQuickScan, icon = { Icon(Icons.Filled.CameraAlt, null) }, text = { Text("Quick Scan") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Pending reviews & tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(state.pendingTasks) { task ->
                LumenCard { Text(task.title) }
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text("Recent documents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(state.recentDocuments) { doc ->
                LumenCard(modifier = Modifier.clickable { onOpenDocument(doc.id) }) {
                    Text(doc.title, fontWeight = FontWeight.Medium)
                    Text(doc.scanMode.name, style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text("Recent notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(state.recentNotes) { note ->
                LumenCard(modifier = Modifier.clickable { onOpenNote(note.id) }) {
                    Text(note.title, fontWeight = FontWeight.Medium)
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text("Recent receipts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(state.recentReceipts) { receipt ->
                LumenCard(modifier = Modifier.clickable { onOpenReceipt(receipt.id) }) {
                    Text(receipt.merchant?.value ?: "Unknown merchant", fontWeight = FontWeight.Medium)
                    Text(receipt.total?.value ?: "-", style = MaterialTheme.typography.bodySmall)
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}
