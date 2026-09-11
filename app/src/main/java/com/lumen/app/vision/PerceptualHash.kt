package com.lumen.app.vision

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real average-hash (aHash) implementation used by the Smart Duplicate Detection Engine.
 * Two images are considered near-duplicates when their Hamming distance is small.
 */
@Singleton
class PerceptualHash @Inject constructor() {

    fun computeHash(bitmap: Bitmap, size: Int = 8): Long {
        val small = Bitmap.createScaledBitmap(bitmap, size, size, true)
        var sum = 0L
        val values = IntArray(size * size)
        var idx = 0
        for (y in 0 until size) {
            for (x in 0 until size) {
                val px = small.getPixel(x, y)
                val r = (px shr 16) and 0xFF
                val g = (px shr 8) and 0xFF
                val b = px and 0xFF
                val gray = (r + g + b) / 3
                values[idx++] = gray
                sum += gray
            }
        }
        val avg = sum / values.size
        var hash = 0L
        for (i in values.indices) {
            hash = hash shl 1
            if (values[i] >= avg) hash = hash or 1L
        }
        return hash
    }

    fun hammingDistance(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)

    /** 0..100, where 100 means identical average-hash. */
    fun similarityPercent(a: Long, b: Long, bits: Int = 64): Int {
        val distance = hammingDistance(a, b)
        return (100 * (bits - distance) / bits).coerceIn(0, 100)
    }
}
