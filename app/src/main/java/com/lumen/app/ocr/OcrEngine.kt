package com.lumen.app.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lumen.app.domain.model.NormalizedRect
import com.lumen.app.domain.model.OcrResult
import com.lumen.app.domain.model.OcrTextBlock
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class OcrScript { LATIN, CHINESE }

/**
 * Thin, real wrapper around Google ML Kit's on-device Text Recognition v2.
 *
 * IMPORTANT / HONEST LIMITATION: ML Kit's on-device text recognizer ships Latin, Chinese,
 * Japanese, Korean and Devanagari script models — there is currently no on-device Arabic/Persian
 * script model. For Persian-script documents this engine will therefore return low-confidence or
 * empty results for the Persian text itself, while still recognising any Latin/numeric characters
 * present. The [OcrScript] enum and provider structure here is intentionally pluggable so a cloud
 * OCR provider (e.g. Google Cloud Vision, which does support Arabic/Persian) can be wired in later
 * as an *optional* provider behind the same interface — never silently, and only after the
 * cloud-consent flow in the Privacy Center.
 */
@Singleton
class OcrEngine @Inject constructor() {

    private val latinRecognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    private val chineseRecognizer by lazy { TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build()) }

    suspend fun recognize(bitmap: Bitmap, script: OcrScript, sourceImageId: String): OcrResult {
        val input = InputImage.fromBitmap(bitmap, 0)
        val recognizer = if (script == OcrScript.CHINESE) chineseRecognizer else latinRecognizer
        val visionText = runRecognition(recognizer, input)
        return mapToOcrResult(visionText, sourceImageId, bitmap.width, bitmap.height)
    }

    private suspend fun runRecognition(
        recognizer: com.google.mlkit.vision.text.TextRecognizer,
        input: InputImage
    ): Text = suspendCancellableCoroutine { cont ->
        recognizer.process(input)
            .addOnSuccessListener { result -> cont.resume(result) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    private fun mapToOcrResult(text: Text, sourceImageId: String, imgW: Int, imgH: Int): OcrResult {
        val blocks = mutableListOf<OcrTextBlock>()
        var blockIdx = 0
        for (block in text.textBlocks) {
            var lineIdx = 0
            for (line in block.lines) {
                val box = line.boundingBox
                val confidence = estimateLineConfidence(line)
                blocks += OcrTextBlock(
                    id = UUID.randomUUID().toString(),
                    text = line.text,
                    boundingBox = box?.let {
                        NormalizedRect(
                            left = it.left.toFloat() / imgW,
                            top = it.top.toFloat() / imgH,
                            right = it.right.toFloat() / imgW,
                            bottom = it.bottom.toFloat() / imgH,
                        )
                    } ?: NormalizedRect(0f, 0f, 0f, 0f),
                    confidence = confidence,
                    languageHint = line.recognizedLanguage,
                    lineIndex = lineIdx,
                    blockIndex = blockIdx,
                )
                lineIdx++
            }
            blockIdx++
        }
        val avgConfidence = if (blocks.isEmpty()) 0f else blocks.map { it.confidence }.average().toFloat()
        return OcrResult(
            id = UUID.randomUUID().toString(),
            sourceImageId = sourceImageId,
            fullText = text.text,
            blocks = blocks,
            detectedLanguage = text.textBlocks.firstOrNull()?.recognizedLanguage,
            averageConfidence = avgConfidence,
            createdAtEpochMs = System.currentTimeMillis(),
        )
    }

    /**
     * ML Kit v2 does not expose a per-character/line confidence score directly on-device, so we
     * derive a practical proxy: shorter, symbol-heavy or single-character lines are penalised
     * slightly, since those are the lines most likely to be OCR noise. This keeps the Confidence
     * & Review system meaningful without pretending to a precision ML Kit doesn't report.
     */
    private fun estimateLineConfidence(line: Text.Line): Float {
        val len = line.text.trim().length
        val alnumRatio = if (len == 0) 0f else line.text.count { it.isLetterOrDigit() }.toFloat() / len
        val lengthScore = (len.coerceAtMost(12) / 12f)
        return (0.5f + 0.3f * alnumRatio + 0.2f * lengthScore).coerceIn(0.1f, 0.99f)
    }
}
