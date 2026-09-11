package com.lumen.app.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun SearchScreen(
    onBack: () -> Unit,
    onOpenNote: (String) -> Unit,
    onOpenDocument: (String) -> Unit,
    onOpenReceipt: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Scaffold(topBar = { LumenTopBar(title = "Search", onBack = onBack) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search notes, documents, receipts…") },
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))

            val isEmpty = results.notes.isEmpty() && results.documents.isEmpty() && results.receipts.isEmpty()
            if (query.isBlank()) {
                EmptyState("Search everything", "Notes, OCR text, documents and receipts — all indexed locally on your device.")
            } else if (isEmpty) {
                EmptyState("No results", "Try a different keyword.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (results.notes.isNotEmpty()) {
                        item { Text("Notes", fontWeight = FontWeight.SemiBold) }
                        items(results.notes) { note ->
                            LumenCard(modifier = Modifier.clickableItem { onOpenNote(note.id) }) { Text(note.title) }
                        }
                    }
                    if (results.documents.isNotEmpty()) {
                        item { Text("Documents", fontWeight = FontWeight.SemiBold) }
                        items(results.documents) { doc ->
                            LumenCard(modifier = Modifier.clickableItem { onOpenDocument(doc.id) }) { Text(doc.title) }
                        }
                    }
                    if (results.receipts.isNotEmpty()) {
                        item { Text("Receipts", fontWeight = FontWeight.SemiBold) }
                        items(results.receipts) { receipt ->
                            LumenCard(modifier = Modifier.clickableItem { onOpenReceipt(receipt.id) }) {
                                Text(receipt.merchant?.value ?: "Receipt")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.clickableItem(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
