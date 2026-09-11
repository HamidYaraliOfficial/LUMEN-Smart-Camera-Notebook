package com.lumen.app.vision

import android.graphics.Bitmap
import android.graphics.PointF
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Heuristic auto-corner suggestion for the document/board scan boundary.
 *
 * HONEST SCOPE NOTE: this uses a lightweight luminance-gradient sweep (no external CV
 * dependency) to *suggest* the 4 corners of a document against a reasonably contrasting
 * background. It works well for the common case (a light page on a dark desk, or a board with
 * dark border) but is not a full Canny/Hough contour pipeline. The Scan screen always lets the
 * user drag-correct the 4 corners before perspective correction runs, so a suboptimal heuristic
 * guess never blocks accurate output — see ScanViewModel.adjustCorner().
 */
@Singleton
class DocumentEdgeDetector @Inject constructor() {

    fun suggestCorners(bitmap: Bitmap): List<PointF> {
        val w = bitmap.width
        val h = bitmap.height
        val sampleStep = maxOf(1, minOf(w, h) / 200)

        val top = scanEdge(bitmap, w, h, sampleStep, fromTop = true)
        val bottom = scanEdge(bitmap, w, h, sampleStep, fromTop = false)
        val marginX = (w * 0.04f)
        val marginTop = top
        val marginBottom = bottom

        return listOf(
            PointF(marginX, marginTop),                 // TL
            PointF(w - marginX, marginTop),              // TR
            PointF(w - marginX, marginBottom),           // BR
            PointF(marginX, marginBottom),                // BL
        )
    }

    private fun scanEdge(bitmap: Bitmap, w: Int, h: Int, step: Int, fromTop: Boolean): Float {
        val centerX = w / 2
        var prevLuma = luminanceAt(bitmap, centerX, if (fromTop) 0 else h - 1)
        val range = if (fromTop) 0 until h step step else (h - 1) downTo 0 step step
        for (y in range) {
            val luma = luminanceAt(bitmap, centerX, y)
            if (abs(luma - prevLuma) > 40) {
                return y.toFloat()
            }
            prevLuma = luma
        }
        return if (fromTop) h * 0.05f else h * 0.95f
    }

    private fun luminanceAt(bitmap: Bitmap, x: Int, y: Int): Int {
        val px = bitmap.getPixel(x.coerceIn(0, bitmap.width - 1), y.coerceIn(0, bitmap.height - 1))
        val r = (px shr 16) and 0xFF
        val g = (px shr 8) and 0xFF
        val b = px and 0xFF
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
}
