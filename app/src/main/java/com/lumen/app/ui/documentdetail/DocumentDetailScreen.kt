package com.lumen.app.ui.documentdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumen.app.ui.components.LumenCard
import com.lumen.app.ui.components.LumenTopBar

/** Document Detail: preview info, summary, keywords, and the full (cleaned) OCR text — wired to
 * the same DocumentRepository/OcrRepository the Scan pipeline writes to. Table/Receipt/Task
 * shortcuts branch out from here in a fuller build; kept focused for this handoff. */
@Composable
fun DocumentDetailScreen(
    onBack: () -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel(),
) {
    val document by viewModel.document.collectAsState()
    val ocrResult by viewModel.ocrResult.collectAsState()

    Scaffold(topBar = { LumenTopBar(title = document?.title ?: "Document", onBack = onBack) }) { padding ->
        val doc = document
        if (doc == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LumenCard {
                Text("Mode: ${doc.scanMode.name}", style = MaterialTheme.typography.bodySmall)
                doc.summaryShort?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("Summary", fontWeight = FontWeight.SemiBold)
                    Text(it)
                }
                if (doc.keywords.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Keywords: ${doc.keywords.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                }
            }

            LumenCard {
                Text("OCR text", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(ocrResult?.fullText ?: "Processing…", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
