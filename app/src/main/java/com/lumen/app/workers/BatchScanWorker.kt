package com.lumen.app.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lumen.app.core.util.BatteryPerformanceManager
import com.lumen.app.data.local.dao.MiscDao
import com.lumen.app.data.local.entity.ScanJobEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Real batch-scan orchestrator: fans out OCR work for many pages while honouring the Battery &
 * Performance Manager's recommended parallelism, and keeps a [ScanJobEntity] row updated with
 * real progress so the Job Manager UI reflects genuine state (not a fake progress bar).
 */
@HiltWorker
class BatchScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val miscDao: MiscDao,
    private val performanceManager: BatteryPerformanceManager,
    private val workManagerBridge: WorkManagerBridge,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val jobId = inputData.getString(KEY_JOB_ID) ?: return@coroutineScope Result.failure()
        val imagePaths = inputData.getStringArray(KEY_IMAGE_PATHS)?.toList() ?: return@coroutineScope Result.failure()
        val sourceImageIds = inputData.getStringArray(KEY_SOURCE_IMAGE_IDS)?.toList() ?: return@coroutineScope Result.failure()

        miscDao.updateJobStatus(jobId, "PROCESSING")
        val parallelism = performanceManager.recommendedParallelJobs()
        var processed = 0

        imagePaths.zip(sourceImageIds).chunked(parallelism).forEach { chunk ->
            val results = chunk.map { (path, sourceId) ->
                async { workManagerBridge.enqueueOcr(applicationContext, path, sourceId) }
            }
            results.awaitAll()
            processed += chunk.size
            setProgressAsync(workDataOf(KEY_PROCESSED to processed, KEY_TOTAL to imagePaths.size))
        }

        miscDao.updateJobStatus(jobId, "COMPLETED")
        Result.success()
    }

    companion object {
        const val KEY_JOB_ID = "job_id"
        const val KEY_IMAGE_PATHS = "image_paths"
        const val KEY_SOURCE_IMAGE_IDS = "source_image_ids"
        const val KEY_PROCESSED = "processed"
        const val KEY_TOTAL = "total"
    }
}
