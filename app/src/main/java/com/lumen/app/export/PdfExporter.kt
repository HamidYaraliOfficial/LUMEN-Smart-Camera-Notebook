package com.lumen.app.export

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real multi-page PDF assembly using Android's built-in PdfDocument (no third-party PDF lib
 * needed for the common "photos -> PDF" case). Each source image is drawn scaled-to-fit onto an
 * A4-proportioned page. Full text-layer ("searchable PDF") support is a documented extension
 * point — see the note in [buildFromImages].
 */
@Singleton
class PdfExporter @Inject constructor() {

    /**
     * Builds a PDF from a list of already-processed page images.
     * NOTE ON "searchable PDF": PdfDocument does not expose a text-layer API. A truly searchable
     * PDF would need to draw the OCR'd words as invisible text runs at their bounding-box
     * coordinates (or use a PDF library with text support, e.g. PDFBox-Android). That is left as
     * a clearly labelled extension point rather than silently shipping an unsearchable file under
     * a "searchable" claim.
     */
    fun buildFromImages(imageFiles: List<File>, outputFile: File): File {
        val document = PdfDocument()
        imageFiles.forEachIndexed { index, file ->
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@forEachIndexed
            val pageWidth = 595 // A4 @ 72dpi
            val pageHeight = 842
            val pageInfo = PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = document.startPage(pageInfo)

            val scale = minOf(pageWidth.toFloat() / bitmap.width, pageHeight.toFloat() / bitmap.height)
            val scaledW = bitmap.width * scale
            val scaledH = bitmap.height * scale
            val left = (pageWidth - scaledW) / 2f
            val top = (pageHeight - scaledH) / 2f

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledW.toInt(), scaledH.toInt(), true)
            page.canvas.drawBitmap(scaledBitmap, left, top, Paint(Paint.ANTI_ALIAS_FLAG))
            document.finishPage(page)
        }
        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()
        return outputFile
    }

    fun mergePdfPageOrder(pages: List<File>, newOrder: List<Int>): List<File> = newOrder.map { pages[it] }
}
