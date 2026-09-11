package com.lumen.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumen.app.core.theme.ConfidenceHigh
import com.lumen.app.core.theme.ConfidenceLow
import com.lumen.app.core.theme.ConfidenceMedium
import com.lumen.app.domain.model.ConfidenceLevel
import com.lumen.app.domain.model.ScanMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumenTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
            }
        },
        actions = actions,
    )
}

@Composable
fun ConfidenceBadge(level: ConfidenceLevel, modifier: Modifier = Modifier) {
    val color = when (level) {
        ConfidenceLevel.HIGH -> ConfidenceHigh
        ConfidenceLevel.MEDIUM -> ConfidenceMedium
        ConfidenceLevel.LOW -> ConfidenceLow
    }
    val label = when (level) {
        ConfidenceLevel.HIGH -> "High"
        ConfidenceLevel.MEDIUM -> "Medium"
        ConfidenceLevel.LOW -> "Needs review"
    }
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanModeChipRow(selected: ScanMode, onSelect: (ScanMode) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ScanMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")) },
            )
        }
    }
}

@Composable
fun LumenCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
