package com.lumen.app.data.local.dao

import androidx.room.*
import com.lumen.app.data.local.entity.OcrResultEntity
import com.lumen.app.data.local.entity.OcrTextBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OcrDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResult(result: OcrResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlocks(blocks: List<OcrTextBlockEntity>)

    @Query("SELECT * FROM ocr_results WHERE id = :id")
    suspend fun getResult(id: String): OcrResultEntity?

    @Query("SELECT * FROM ocr_results WHERE id = :id")
    fun observeResult(id: String): Flow<OcrResultEntity?>

    @Query("SELECT * FROM ocr_blocks WHERE ocrResultId = :ocrResultId ORDER BY blockIndex, lineIndex")
    fun observeBlocks(ocrResultId: String): Flow<List<OcrTextBlockEntity>>

    @Query("SELECT * FROM ocr_blocks WHERE ocrResultId = :ocrResultId AND confidence < :threshold")
    suspend fun getLowConfidenceBlocks(ocrResultId: String, threshold: Float = 0.6f): List<OcrTextBlockEntity>

    @Query(
        """
        SELECT ocr_results.* FROM ocr_results
        JOIN ocr_results_fts ON ocr_results.rowid = ocr_results_fts.rowid
        WHERE ocr_results_fts MATCH :query
        """
    )
    suspend fun searchFts(query: String): List<OcrResultEntity>
}
