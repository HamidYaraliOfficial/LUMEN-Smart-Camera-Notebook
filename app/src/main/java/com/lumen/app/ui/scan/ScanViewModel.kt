package com.lumen.app.ui.scan

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.app.camera.CameraController
import com.lumen.app.core.util.ImageUtils
import com.lumen.app.core.util.LumenStorageManager
import com.lumen.app.domain.model.*
import com.lumen.app.domain.repository.DocumentRepository
import com.lumen.app.domain.repository.OcrRepository
import com.lumen.app.domain.repository.ReceiptRepository
import com.lumen.app.domain.repository.TableRepository
import com.lumen.app.intelligence.*
import com.lumen.app.ocr.OcrEngine
import com.lumen.app.ocr.OcrScript
import com.lumen.app.vision.ImagePreprocessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ScanUiState(
    val selectedMode: ScanMode = ScanMode.DOCUMENT,
    val stage: PipelineStage? = null,
    val isProcessing: Boolean = false,
    val lastDocumentId: String? = null,
    val lastReceiptId: String? = null,
    val lastTableId: String? = null,
    val errorMessage: String? = null,
)

/**
 * Orchestrates the real Capture -> Preprocess -> Detect -> OCR -> Structure -> Clean -> Analyze
 * -> Summarize -> Index -> Save pipeline for a single captured page.
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val cameraController: CameraController,
    private val imagePreprocessor: ImagePreprocessor,
    private val ocrEngine: OcrEngine,
    private val cleanupEngine: AiCleanupEngine,
    private val classificationEngine: SmartClassificationEngine,
    private val summarizationEngine: SummarizationEngine,
    private val tableExtractionEngine: TableExtractionEngine,
    private val receiptParsingEngine: ReceiptParsingEngine,
    private val documentRepository: DocumentRepository,
    private val ocrRepository: OcrRepository,
    private val receiptRepository: ReceiptRepository,
    private val tableRepository: TableRepository,
    private val storageManager: LumenStorageManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun selectMode(mode: ScanMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    suspend fun bindCamera(context: Context, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        cameraController.bind(context, lifecycleOwner, previewView)
    }

    fun setTorch(enabled: Boolean) = cameraController.setTorchEnabled(enabled)
    fun setZoom(ratio: Float) = cameraController.setZoomRatio(ratio)

    fun captureAndProcess(context: Context, notebookId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, stage = PipelineStage.CAPTURE, errorMessage = null)
            try {
                val file = cameraController.capturePhoto(context, storageManager.originalImagesDir())

                _uiState.value = _uiState.value.copy(stage = PipelineStage.PREPROCESS)
                var bitmap = ImageUtils.decodeSampled(file, 2200, 2200)
                bitmap = ImageUtils.correctOrientation(file, bitmap)
                val enhanced = imagePreprocessor.standardDocumentEnhance(bitmap)
                val processedFile = java.io.File(storageManager.processedImagesDir(), "processed_${file.name}")
                ImageUtils.saveBitmap(enhanced, processedFile)

                _uiState.value = _uiState.value.copy(stage = PipelineStage.OCR)
                val sourceImageId = UUID.randomUUID().toString()
                val ocrResult = ocrEngine.recognize(enhanced, OcrScript.LATIN, sourceImageId)

                _uiState.value = _uiState.value.copy(stage = PipelineStage.CLEAN)
                val cleaned = cleanupEngine.clean(ocrResult.fullText)
                val cleanedResult = ocrResult.copy(fullText = cleaned.cleaned)

                _uiState.value = _uiState.value.copy(stage = PipelineStage.STRUCTURE)
                val effectiveMode = if (_uiState.value.selectedMode == ScanMode.FREE_SCAN) {
                    classificationEngine.classify(cleanedResult)
                } else _uiState.value.selectedMode

                _uiState.value = _uiState.value.copy(stage = PipelineStage.ANALYZE)
                val summaryShort = summarizationEngine.summarize(cleanedResult.fullText, SummaryDepth.SHORT)
                val summaryMedium = summarizationEngine.summarize(cleanedResult.fullText, SummaryDepth.MEDIUM)
                val keywords = summarizationEngine.extractKeywords(cleanedResult.fullText)

                _uiState.value = _uiState.value.copy(stage = PipelineStage.INDEX)
                ocrRepository.save(cleanedResult)

                val documentId = UUID.randomUUID().toString()
                val page = DocumentPage(
                    id = UUID.randomUUID().toString(),
                    pageIndex = 0,
                    originalImageUri = file.absolutePath,
                    processedImageUri = processedFile.absolutePath,
                    ocrResultId = cleanedResult.id,
                )
                val document = LumenDocument(
                    id = documentId,
                    notebookId = notebookId,
                    title = summaryShort.take(60).ifBlank { "Scan ${System.currentTimeMillis()}" },
                    scanMode = effectiveMode,
                    pages = listOf(page),
                    summaryShort = summaryShort,
                    summaryMedium = summaryMedium,
                    summaryDeep = null,
                    keywords = keywords,
                    tags = emptyList(),
                    pdfUri = null,
                    createdAtEpochMs = System.currentTimeMillis(),
                    updatedAtEpochMs = System.currentTimeMillis(),
                )

                _uiState.value = _uiState.value.copy(stage = PipelineStage.SAVE)
                documentRepository.upsert(document)

                var receiptId: String? = null
                var tableId: String? = null

                if (effectiveMode == ScanMode.RECEIPT) {
                    val receipt = receiptParsingEngine.parse(notebookId, documentId, cleanedResult)
                    receiptRepository.upsert(receipt)
                    receiptId = receipt.id
                }
                if (effectiveMode == ScanMode.TABLE) {
                    val table = tableExtractionEngine.extract(documentId, document.title, cleanedResult.blocks)
                    if (table != null) {
                        tableRepository.upsert(table)
                        tableId = table.id
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isProcessing = false, stage = null,
                    lastDocumentId = documentId, lastReceiptId = receiptId, lastTableId = tableId,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isProcessing = false, stage = null, errorMessage = e.message ?: "Scan failed")
            }
        }
    }

    override fun onCleared() {
        cameraController.unbind()
        super.onCleared()
    }
}
