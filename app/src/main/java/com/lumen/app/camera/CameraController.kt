package com.lumen.app.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Real CameraX pipeline wrapper: Preview + ImageCapture + (optional) ImageAnalysis for Live OCR.
 * Exposes focus, zoom, torch and an adaptive analysis-frame-rate knob so the Live OCR overlay
 * doesn't peg the CPU/GPU (see [setAnalysisIntervalMs]).
 */
@Singleton
class CameraController @Inject constructor() {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var lastAnalysisAtMs = 0L
    private var analysisIntervalMs = 700L // adaptive: raised on low battery / thermal throttling

    suspend fun bind(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onFrameForAnalysis: ((ImageProxy) -> Unit)? = null,
    ) {
        val provider = getOrCreateProvider(context)
        cameraProvider = provider

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        val analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        if (onFrameForAnalysis != null) {
            analysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(context)) { proxy ->
                val now = System.currentTimeMillis()
                if (now - lastAnalysisAtMs >= analysisIntervalMs) {
                    lastAnalysisAtMs = now
                    onFrameForAnalysis(proxy)
                } else {
                    proxy.close()
                }
            }
        }
        imageAnalysis = analysisUseCase

        provider.unbindAll()
        val useCases = mutableListOf<UseCase>(preview, imageCapture!!)
        if (onFrameForAnalysis != null) useCases += analysisUseCase

        camera = provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            *useCases.toTypedArray()
        )
    }

    private suspend fun getOrCreateProvider(context: Context): ProcessCameraProvider =
        suspendCancellableCoroutine { cont ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                try {
                    cont.resume(future.get())
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(context))
        }

    suspend fun capturePhoto(context: Context, outputDir: File): File =
        suspendCancellableCoroutine { cont ->
            val capture = imageCapture ?: return@suspendCancellableCoroutine cont.resumeWithException(
                IllegalStateException("Camera not bound")
            )
            val file = File(outputDir, "LUMEN_${System.currentTimeMillis()}.jpg")
            val options = ImageCapture.OutputFileOptions.Builder(file).build()
            capture.takePicture(
                options,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        cont.resume(file)
                    }
                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraController", "Capture failed", exception)
                        cont.resumeWithException(exception)
                    }
                }
            )
        }

    fun setTorchEnabled(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }

    fun setZoomRatio(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio)
    }

    fun setLinearZoom(value: Float) {
        camera?.cameraControl?.setLinearZoom(value.coerceIn(0f, 1f))
    }

    fun focusAt(meteringPointFactory: MeteringPointFactory, x: Float, y: Float) {
        val point = meteringPointFactory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    /** Called by the Battery & Performance Manager to widen/narrow the Live-OCR sampling interval. */
    fun setAnalysisIntervalMs(intervalMs: Long) {
        analysisIntervalMs = intervalMs.coerceIn(200L, 3000L)
    }

    fun unbind() {
        cameraProvider?.unbindAll()
    }
}
