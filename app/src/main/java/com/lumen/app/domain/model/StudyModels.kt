package com.lumen.app.domain.model

data class Flashcard(
    val id: String,
    val notebookId: String,
    val sourceDocumentId: String?,
    val question: String,
    val answer: String,
    val tags: List<String>,
    val timesReviewed: Int = 0,
    val lastReviewedAtEpochMs: Long? = null,
    val createdAtEpochMs: Long,
)

data class StudyConcept(
    val id: String,
    val term: String,
    val definition: String,
    val sourceDocumentId: String?,
)

data class StudyQuestion(
    val id: String,
    val question: String,
    val answer: String?,
    val sourceDocumentId: String?,
)

data class ExtractedQuote(
    val id: String,
    val text: String,
    val possibleAuthor: String?,
    val sourceDocumentId: String?,
)
