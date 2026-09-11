package com.lumen.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun decodeSampled(file: File, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(file.absolutePath, options)
            ?: throw IllegalStateException("Could not decode image: ${file.absolutePath}")
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun correctOrientation(file: File, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap
        val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun saveBitmap(bitmap: Bitmap, destination: File, quality: Int = 90) {
        FileOutputStream(destination).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
    }

    fun createThumbnail(bitmap: Bitmap, maxDimension: Int = 256): Bitmap {
        val ratio = minOf(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height, 1f)
        val w = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val h = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }
}
