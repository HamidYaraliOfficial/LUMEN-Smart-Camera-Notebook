package com.lumen.app.data.repository

import com.lumen.app.data.local.dao.OcrDao
import com.lumen.app.domain.model.OcrResult
import com.lumen.app.domain.model.OcrTextBlock
import com.lumen.app.domain.repository.OcrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OcrRepositoryImpl @Inject constructor(
    private val dao: OcrDao
) : OcrRepository {
    override suspend fun save(result: OcrResult) {
        dao.upsertResult(
            com.lumen.app.data.local.entity.OcrResultEntity(
                id = result.id, sourceImageId = result.sourceImageId, fullText = result.fullText,
                detectedLanguage = result.detectedLanguage, averageConfidence = result.averageConfidence,
                createdAtEpochMs = result.createdAtEpochMs,
            )
        )
        dao.upsertBlocks(result.blocks.map { it.toEntity(result.id) })
    }

    override suspend fun getResult(id: String): OcrResult? {
        val entity = dao.getResult(id) ?: return null
        val blocks = dao.observeBlocks(id)
        return entity.toDomain(emptyList()) // callers typically use observeResult+observeBlocks together
    }

    override fun observeResult(id: String): Flow<OcrResult?> = dao.observeResult(id).map { it?.toDomain(emptyList()) }

    override fun observeBlocks(ocrResultId: String): Flow<List<OcrTextBlock>> =
        dao.observeBlocks(ocrResultId).map { list -> list.map { it.toDomain() } }
}
