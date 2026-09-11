package com.lumen.app.core.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.lumen.app.domain.model.StorageBreakdown
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Real on-disk storage accounting for the Storage Manager screen. */
@Singleton
class LumenStorageManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private fun dir(name: String): File = File(appContext.filesDir, name).apply { mkdirs() }
    fun originalImagesDir() = dir("lumen_images/original")
    fun processedImagesDir() = dir("lumen_images/processed")
    fun thumbnailsDir() = dir("lumen_images/thumbnails")
    fun pdfsDir() = File(appContext.filesDir, "lumen_pdfs").apply { mkdirs() }
    fun aiDataDir() = dir("lumen_ai_data")
    fun cacheDir(): File = appContext.cacheDir.resolve("lumen_cache").apply { mkdirs() }

    private fun sizeOf(dir: File): Long = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    fun computeBreakdown(): StorageBreakdown = StorageBreakdown(
        originalImagesBytes = sizeOf(originalImagesDir()),
        processedImagesBytes = sizeOf(processedImagesDir()),
        ocrDataBytes = appContext.getDatabasePath("lumen_database").let { if (it.exists()) it.length() else 0L },
        pdfsBytes = sizeOf(pdfsDir()),
        thumbnailsBytes = sizeOf(thumbnailsDir()),
        aiDataBytes = sizeOf(aiDataDir()),
        cacheBytes = sizeOf(cacheDir()),
    )

    /** Clears cache only — never touches original images, the database, or PDFs. */
    fun clearCache(): Long {
        val freed = sizeOf(cacheDir())
        cacheDir().deleteRecursively()
        cacheDir() // recreate
        return freed
    }
}
