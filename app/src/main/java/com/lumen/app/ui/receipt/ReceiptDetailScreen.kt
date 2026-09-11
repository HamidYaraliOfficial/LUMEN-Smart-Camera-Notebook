package com.lumen.app.ui.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumen.app.domain.model.ConfidenceLevel
import com.lumen.app.ui.components.ConfidenceBadge
import com.lumen.app.ui.components.LumenCard
import com.lumen.app.ui.components.LumenTopBar

@Composable
fun ReceiptDetailScreen(
    onBack: () -> Unit,
    viewModel: ReceiptViewModel = hiltViewModel(),
) {
    val receipt by viewModel.receipt.collectAsState()

    Scaffold(topBar = { LumenTopBar(title = "Receipt", onBack = onBack) }) { padding ->
        val r = receipt
        if (r == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    LumenCard {
                        FieldRow("Merchant", r.merchant?.value, r.merchant?.confidenceLevel)
                        FieldRow("Date", r.date?.value, r.date?.confidenceLevel)
                        FieldRow("Total", r.total?.value, r.total?.confidenceLevel)
                        FieldRow("Tax", r.tax?.value, r.tax?.confidenceLevel)
                        FieldRow("Currency", r.currency?.value, null)
                        FieldRow("Payment method", r.paymentMethod?.value, null)
                    }
                }
                if (r.items.isNotEmpty()) {
                    item { Text("Items", fontWeight = FontWeight.SemiBold) }
                    items(r.items) { item ->
                        LumenCard {
                            Text(item.name, fontWeight = FontWeight.Medium)
                            Text("${item.quantity ?: 1} × ${item.unitPrice ?: 0.0} = ${item.lineTotal ?: 0.0}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldRow(label: String, value: String?, confidence: ConfidenceLevel?) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value ?: "—", fontWeight = FontWeight.Medium)
        }
        confidence?.let { ConfidenceBadge(it) }
    }
}
