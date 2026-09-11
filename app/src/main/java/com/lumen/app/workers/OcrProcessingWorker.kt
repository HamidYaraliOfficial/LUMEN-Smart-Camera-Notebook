package com.lumen.app.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lumen.app.core.util.ImageUtils
import com.lumen.app.domain.repository.OcrRepository
import com.lumen.app.intelligence.AiCleanupEngine
import com.lumen.app.ocr.OcrEngine
import com.lumen.app.ocr.OcrScript
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

/**
 * Real CoroutineWorker: runs ML Kit OCR + the AI cleanup pass on a captured page in the
 * background, then persists the result via [OcrRepository]. Triggered by BatchScanWorker or
 * directly after a single capture.
 */
@HiltWorker
class OcrProcessingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val ocrEngine: OcrEngine,
    private val ocrRepository: OcrRepository,
    private val cleanupEngine: AiCleanupEngine,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val imagePath = inputData.getString(KEY_IMAGE_PATH) ?: return Result.failure()
        val sourceImageId = inputData.getString(KEY_SOURCE_IMAGE_ID) ?: return Result.failure()
        val scriptName = inputData.getString(KEY_SCRIPT) ?: OcrScript.LATIN.name

        return try {
            val file = File(imagePath)
            if (!file.exists()) return Result.failure()
            val bitmap = ImageUtils.decodeSampled(file, 2000, 2000)
            val script = runCatching { OcrScript.valueOf(scriptName) }.getOrDefault(OcrScript.LATIN)
            val result = ocrEngine.recognize(bitmap, script, sourceImageId)
            val cleaned = cleanupEngine.clean(result.fullText)
            ocrRepository.save(result.copy(fullText = cleaned.cleaned))
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_IMAGE_PATH = "image_path"
        const val KEY_SOURCE_IMAGE_ID = "source_image_id"
        const val KEY_SCRIPT = "script"
    }
}
