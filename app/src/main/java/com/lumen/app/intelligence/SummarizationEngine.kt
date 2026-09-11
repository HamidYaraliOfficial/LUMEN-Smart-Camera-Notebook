package com.lumen.app.intelligence

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

enum class SummaryDepth { SHORT, MEDIUM, DEEP }

/**
 * On-device extractive summariser (word-frequency sentence scoring, a real and well-understood
 * offline NLP technique — no cloud call required). It is intentionally simpler than a large
 * language model, and is clearly presented to the user as an on-device summary; when the user
 * opts in to the optional Cloud AI provider (see CloudAiProvider), higher-quality abstractive
 * summaries can be requested instead through the same interface.
 */
@Singleton
class SummarizationEngine @Inject constructor() {

    private val stopWords = setOf(
        "the", "a", "an", "and", "or", "of", "to", "in", "on", "for", "is", "are", "was", "were", "with", "this", "that",
        "و", "در", "به", "از", "که", "این", "را", "با", "است", "برای",
        "的", "了", "和", "是", "在", "有", "我", "你", "他", "她", "它", "这", "那",
    )

    fun summarize(text: String, depth: SummaryDepth): String {
        val sentences = splitSentences(text)
        if (sentences.size <= 2) return text.trim()

        val wordFreq = mutableMapOf<String, Int>()
        for (sentence in sentences) {
            for (word in tokenize(sentence)) {
                if (word.length < 2 || word.lowercase() in stopWords) continue
                wordFreq[word.lowercase()] = (wordFreq[word.lowercase()] ?: 0) + 1
            }
        }

        val scored = sentences.mapIndexed { index, sentence ->
            val words = tokenize(sentence)
            val score = words.sumOf { wordFreq[it.lowercase()] ?: 0 }.toDouble() / (words.size + 1)
            // slight boost for sentences near the start (titles/topic sentences)
            val positionBoost = if (index == 0) 1.15 else 1.0
            index to score * positionBoost
        }

        val targetCount = when (depth) {
            SummaryDepth.SHORT -> min(2, sentences.size)
            SummaryDepth.MEDIUM -> min(5, sentences.size)
            SummaryDepth.DEEP -> min(10, sentences.size)
        }

        val topIndices = scored.sortedByDescending { it.second }.take(targetCount).map { it.first }.sorted()
        return topIndices.joinToString(" ") { sentences[it].trim() }
    }

    fun extractKeywords(text: String, max: Int = 10): List<String> {
        val freq = mutableMapOf<String, Int>()
        for (word in tokenize(text)) {
            val w = word.lowercase()
            if (w.length < 3 || w in stopWords) continue
            freq[w] = (freq[w] ?: 0) + 1
        }
        return freq.entries.sortedByDescending { it.value }.take(max).map { it.key }
    }

    private fun splitSentences(text: String): List<String> =
        text.split(Regex("(?<=[.!?。！？])\\s+")).filter { it.isNotBlank() }

    private fun tokenize(text: String): List<String> =
        text.split(Regex("[\\s,.:;!?()\\[\\]{}\"'،؛。，、]+")).filter { it.isNotBlank() }
}
