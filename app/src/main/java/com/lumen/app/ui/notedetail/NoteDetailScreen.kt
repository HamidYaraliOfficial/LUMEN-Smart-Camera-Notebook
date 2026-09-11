package com.lumen.app.ui.notedetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumen.app.ui.components.LumenTopBar

@Composable
fun NoteDetailScreen(
    onBack: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel(),
) {
    val note by viewModel.note.collectAsState()
    var bodyText by remember { mutableStateOf("") }

    LaunchedEffect(note?.id) {
        note?.let { bodyText = it.bodyMarkdown }
    }

    Scaffold(
        topBar = {
            LumenTopBar(title = note?.title ?: "Note", onBack = onBack) {
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(if (note?.isFavorite == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null)
                }
                IconButton(onClick = { viewModel.delete(onBack) }) {
                    Icon(Icons.Filled.Delete, null)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = bodyText,
                onValueChange = { bodyText = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp),
                label = { Text("Note content") },
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { viewModel.updateBody(bodyText) }) { Text("Save") }
        }
    }
}
