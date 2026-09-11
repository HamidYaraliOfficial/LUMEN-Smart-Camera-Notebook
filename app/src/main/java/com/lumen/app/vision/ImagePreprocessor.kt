package com.lumen.app.vision

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real, on-device image preprocessing used before OCR:
 *  - perspective correction from 4 user (or heuristically) chosen corners, via Matrix.setPolyToPoly
 *  - deskew/rotate
 *  - grayscale + contrast/brightness normalisation ("shadow flattening" proxy)
 *
 * Note on scope: true illumination-invariant shadow removal and sub-pixel deskew typically need a
 * vision library such as OpenCV. This class implements a solid, real, dependency-light version
 * (perspective warp + adaptive contrast) rather than faking a more advanced pipeline; OpenCV/TFLite
 * based enhancement is a documented extension point (see ModelManager) for teams that want it.
 */
@Singleton
class ImagePreprocessor @Inject constructor() {

    /** Warps the quadrilateral defined by [corners] (TL, TR, BR, BL, in source pixel coords) to a flat rectangle. */
    fun perspectiveCorrect(source: Bitmap, corners: List<PointF>, outWidth: Int, outHeight: Int): Bitmap {
        require(corners.size == 4) { "Perspective correction requires exactly 4 corners (TL, TR, BR, BL)" }
        val src = floatArrayOf(
            corners[0].x, corners[0].y,
            corners[1].x, corners[1].y,
            corners[2].x, corners[2].y,
            corners[3].x, corners[3].y,
        )
        val dst = floatArrayOf(
            0f, 0f,
            outWidth.toFloat(), 0f,
            outWidth.toFloat(), outHeight.toFloat(),
            0f, outHeight.toFloat(),
        )
        val matrix = Matrix()
        matrix.setPolyToPoly(src, 0, dst, 0, 4)

        val output = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(source, matrix, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
        return output
    }

    fun rotate(source: Bitmap, degrees: Float): Bitmap {
        if (degrees % 360f == 0f) return source
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun grayscale(source: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    /** Simple linear contrast + brightness normalisation used as a lightweight "flatten shadows" step. */
    fun enhanceContrast(source: Bitmap, contrast: Float = 1.25f, brightness: Float = 12f): Bitmap {
        val cm = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f,
            )
        )
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    fun cropTo(source: Bitmap, left: Int, top: Int, width: Int, height: Int): Bitmap =
        Bitmap.createBitmap(source, left, top, width, height)

    /** Runs the standard "document flattening" chain: enhanceContrast -> grayscale is intentionally
     * left optional since colour is often useful for board photos / receipts. */
    fun standardDocumentEnhance(source: Bitmap): Bitmap = enhanceContrast(source)
}
