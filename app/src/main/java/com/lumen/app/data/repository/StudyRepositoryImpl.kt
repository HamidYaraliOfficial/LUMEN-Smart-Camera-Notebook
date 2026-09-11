package com.lumen.app.data.repository

import com.lumen.app.data.local.dao.StudyDao
import com.lumen.app.domain.model.Flashcard
import com.lumen.app.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudyRepositoryImpl @Inject constructor(
    private val dao: StudyDao
) : StudyRepository {
    override fun observeFlashcards(notebookId: String): Flow<List<Flashcard>> =
        dao.observeByNotebook(notebookId).map { list -> list.map { it.toDomain() } }

    override fun observeAllFlashcards(): Flow<List<Flashcard>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsertFlashcards(cards: List<Flashcard>) = dao.upsertFlashcards(cards.map { it.toEntity() })
    override suspend fun markReviewed(id: String) = dao.markReviewed(id, System.currentTimeMillis())
    override suspend fun delete(id: String) = dao.delete(id)
}
