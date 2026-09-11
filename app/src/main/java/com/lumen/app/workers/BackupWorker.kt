package com.lumen.app.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lumen.app.core.util.LumenStorageManager
import com.lumen.app.data.local.dao.MiscDao
import com.lumen.app.data.local.entity.BackupRecordEntity
import com.lumen.app.security.CryptoManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Real local backup: zips the Room database file + original images, optionally AES/GCM-encrypts
 * the archive via [CryptoManager], and records it so it shows up in Settings -> Backup & Restore. */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val storageManager: LumenStorageManager,
    private val cryptoManager: CryptoManager,
    private val miscDao: MiscDao,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val encrypt = inputData.getBoolean(KEY_ENCRYPT, true)
            val backupDir = File(applicationContext.filesDir, "lumen_backups").apply { mkdirs() }
            val zipFile = File(backupDir, "lumen_backup_${System.currentTimeMillis()}.zip")

            ZipOutputStream(zipFile.outputStream()).use { zos ->
                val dbFile = applicationContext.getDatabasePath("lumen_database")
                if (dbFile.exists()) addFileToZip(zos, dbFile, "database/${dbFile.name}")
                storageManager.originalImagesDir().walkTopDown().filter { it.isFile }.forEach { file ->
                    addFileToZip(zos, file, "images/${file.name}")
                }
            }

            val finalFile = if (encrypt) {
                val encryptedFile = File(backupDir, "${zipFile.name}.enc")
                val payload = cryptoManager.encrypt(zipFile.readBytes())
                encryptedFile.outputStream().use { out ->
                    out.write(payload.iv.size)
                    out.write(payload.iv)
                    out.write(payload.ciphertext)
                }
                zipFile.delete()
                encryptedFile
            } else zipFile

            miscDao.upsertBackupRecord(
                BackupRecordEntity(
                    id = UUID.randomUUID().toString(),
                    fileUri = finalFile.absolutePath,
                    sizeBytes = finalFile.length(),
                    isEncrypted = encrypt,
                    createdAtEpochMs = System.currentTimeMillis(),
                )
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun addFileToZip(zos: ZipOutputStream, file: File, entryName: String) {
        zos.putNextEntry(ZipEntry(entryName))
        file.inputStream().use { it.copyTo(zos) }
        zos.closeEntry()
    }

    companion object {
        const val KEY_ENCRYPT = "encrypt"
    }
}
