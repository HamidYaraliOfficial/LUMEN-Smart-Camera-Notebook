package com.lumen.app.workers

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import javax.inject.Inject
import javax.inject.Singleton

/** Small helper to enqueue [OcrProcessingWorker] jobs from other workers/use-cases uniformly. */
@Singleton
class WorkManagerBridge @Inject constructor() {
    suspend fun enqueueOcr(context: Context, imagePath: String, sourceImageId: String, script: String = "LATIN") {
        val request = OneTimeWorkRequestBuilder<OcrProcessingWorker>()
            .setInputData(
                workDataOf(
                    OcrProcessingWorker.KEY_IMAGE_PATH to imagePath,
                    OcrProcessingWorker.KEY_SOURCE_IMAGE_ID to sourceImageId,
                    OcrProcessingWorker.KEY_SCRIPT to script,
                )
            )
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
