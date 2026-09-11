package com.lumen.app.intelligence

import com.lumen.app.domain.model.Flashcard
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real, offline Flashcard generator. Two strategies, both deterministic and inspectable:
 *  1. Q/A pattern detection — lines already formatted as "Q: ... A: ..." or "Question / Answer".
 *  2. Definition pattern detection — "Term: definition", "Term is/means definition", "Term — definition".
 * The result is always editable by the user afterwards.
 */
@Singleton
class FlashcardGenerationEngine @Inject constructor() {

    private val qaRegex = Regex("(?i)^(q|question|سوال|问题)\\s*[:.]\\s*(.+)$")
    private val aRegex = Regex("(?i)^(a|answer|پاسخ|答案)\\s*[:.]\\s*(.+)$")
    private val definitionRegex = Regex("^(.{2,60}?)\\s*(?:[:：-]|—|is|means|یعنی|به معنای)\\s+(.{3,200})$")

    fun generate(notebookId: String, sourceDocumentId: String?, text: String, maxCards: Int = 15): List<Flashcard> {
        val cards = mutableListOf<Flashcard>()
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        var i = 0
        while (i < lines.size) {
            val qMatch = qaRegex.find(lines[i])
            if (qMatch != null && i + 1 < lines.size) {
                val aMatch = aRegex.find(lines[i + 1])
                if (aMatch != null) {
                    cards += buildCard(notebookId, sourceDocumentId, qMatch.groupValues[2], aMatch.groupValues[2])
                    i += 2
                    continue
                }
            }
            i++
        }

        if (cards.size < maxCards) {
            for (line in lines) {
                if (cards.size >= maxCards) break
                val defMatch = definitionRegex.find(line) ?: continue
                val term = defMatch.groupValues[1].trim()
                val definition = defMatch.groupValues[2].trim()
                if (term.split(" ").size <= 6 && definition.length in 5..220) {
                    cards += buildCard(notebookId, sourceDocumentId, "What is $term?", definition)
                }
            }
        }

        return cards.take(maxCards)
    }

    private fun buildCard(notebookId: String, sourceDocumentId: String?, question: String, answer: String) = Flashcard(
        id = UUID.randomUUID().toString(),
        notebookId = notebookId,
        sourceDocumentId = sourceDocumentId,
        question = question.trim(),
        answer = answer.trim(),
        tags = emptyList(),
        createdAtEpochMs = System.currentTimeMillis(),
    )
}
