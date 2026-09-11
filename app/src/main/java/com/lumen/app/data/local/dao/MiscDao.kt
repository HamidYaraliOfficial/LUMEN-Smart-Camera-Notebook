package com.lumen.app.data.local.dao

import androidx.room.*
import com.lumen.app.data.local.entity.BackupRecordEntity
import com.lumen.app.data.local.entity.BookmarkedCodeEntity
import com.lumen.app.data.local.entity.ScanJobEntity
import com.lumen.app.data.local.entity.SmartClipboardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MiscDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(code: BookmarkedCodeEntity)

    @Query("SELECT * FROM bookmarked_codes ORDER BY savedAtEpochMs DESC")
    fun observeBookmarks(): Flow<List<BookmarkedCodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClipboardEntry(entry: SmartClipboardEntity)

    @Query("SELECT * FROM smart_clipboard ORDER BY copiedAtEpochMs DESC LIMIT 100")
    fun observeClipboardHistory(): Flow<List<SmartClipboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScanJob(job: ScanJobEntity)

    @Query("SELECT * FROM scan_jobs WHERE status IN ('QUEUED','PROCESSING','REVIEW') ORDER BY createdAtEpochMs DESC")
    fun observeActiveJobs(): Flow<List<ScanJobEntity>>

    @Query("UPDATE scan_jobs SET status = :status WHERE id = :id")
    suspend fun updateJobStatus(id: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBackupRecord(record: BackupRecordEntity)

    @Query("SELECT * FROM backup_records ORDER BY createdAtEpochMs DESC")
    fun observeBackups(): Flow<List<BackupRecordEntity>>
}
