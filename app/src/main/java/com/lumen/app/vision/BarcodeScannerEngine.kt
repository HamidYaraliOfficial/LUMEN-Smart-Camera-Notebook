package com.lumen.app.vision

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.lumen.app.domain.model.BookmarkedCode
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Real ML Kit barcode/QR scanning — used both in Live Camera mode and on captured stills. */
@Singleton
class BarcodeScannerEngine @Inject constructor() {
    private val scanner by lazy { BarcodeScanning.getClient() }

    suspend fun scan(bitmap: Bitmap): List<BookmarkedCode> {
        val input = InputImage.fromBitmap(bitmap, 0)
        val barcodes: List<Barcode> = suspendCancellableCoroutine { cont ->
            scanner.process(input)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
        return barcodes.mapNotNull { bc ->
            val raw = bc.rawValue ?: return@mapNotNull null
            BookmarkedCode(
                id = UUID.randomUUID().toString(),
                rawValue = raw,
                format = formatName(bc.format),
                isUrl = bc.valueType == Barcode.TYPE_URL,
                savedAtEpochMs = System.currentTimeMillis(),
            )
        }
    }

    private fun formatName(format: Int): String = when (format) {
        Barcode.FORMAT_QR_CODE -> "QR_CODE"
        Barcode.FORMAT_EAN_13 -> "EAN_13"
        Barcode.FORMAT_EAN_8 -> "EAN_8"
        Barcode.FORMAT_CODE_128 -> "CODE_128"
        Barcode.FORMAT_CODE_39 -> "CODE_39"
        Barcode.FORMAT_UPC_A -> "UPC_A"
        Barcode.FORMAT_UPC_E -> "UPC_E"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_AZTEC -> "AZTEC"
        Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
        else -> "UNKNOWN"
    }
}
