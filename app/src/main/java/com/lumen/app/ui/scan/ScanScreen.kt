package com.lumen.app.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumen.app.domain.model.PipelineStage
import com.lumen.app.ui.components.ScanModeChipRow
import kotlinx.coroutines.launch

@Composable
fun ScanScreen(
    notebookId: String,
    onScanComplete: (documentId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var torchOn by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    LaunchedEffect(state.lastDocumentId) {
        state.lastDocumentId?.let { onScanComplete(it) }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { previewView ->
                            scope.launch { viewModel.bindCamera(ctx, lifecycleOwner, previewView) }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Camera permission is required to scan.")
                }
            }

            Column(
                modifier = Modifier.align(Alignment.TopStart).fillMaxWidth().padding(12.dp),
            ) {
                ScanModeChipRow(selected = state.selectedMode, onSelect = viewModel::selectMode)
            }

            if (state.isProcessing) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text(stageLabel(state.stage))
                }
            }

            state.errorMessage?.let { message ->
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text(message) }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    torchOn = !torchOn
                    viewModel.setTorch(torchOn)
                }) { Icon(Icons.Filled.Bolt, contentDescription = "Torch") }

                FloatingActionButton(onClick = { viewModel.captureAndProcess(context, notebookId) }) {
                    Icon(Icons.Filled.Camera, contentDescription = "Capture")
                }

                Spacer(Modifier.width(48.dp))
            }
        }
    }
}

private fun stageLabel(stage: PipelineStage?): String = when (stage) {
    PipelineStage.CAPTURE -> "Capturing…"
    PipelineStage.PREPROCESS -> "Enhancing image…"
    PipelineStage.DETECT -> "Detecting boundaries…"
    PipelineStage.OCR -> "Reading text…"
    PipelineStage.STRUCTURE -> "Structuring content…"
    PipelineStage.CLEAN -> "Cleaning text…"
    PipelineStage.ANALYZE -> "Analyzing…"
    PipelineStage.SUMMARIZE -> "Summarizing…"
    PipelineStage.INDEX -> "Indexing…"
    PipelineStage.SAVE -> "Saving…"
    null -> ""
}
