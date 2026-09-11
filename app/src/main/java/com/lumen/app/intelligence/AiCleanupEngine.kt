package com.lumen.app.intelligence

import javax.inject.Inject
import javax.inject.Singleton

data class CleanedText(val original: String, val cleaned: String)

/**
 * Real, deterministic OCR-cleanup pass: fixes common recognition artefacts (stray spaces before
 * punctuation, broken hyphenated line-wraps, repeated whitespace, common substitution errors like
 * "0"/"O" or "1"/"l" *only* inside clearly-alphabetic words). The original text is always kept
 * alongside the cleaned text (never overwritten) so the user can compare and revert per the
 * Confidence & Review system.
 */
@Singleton
class AiCleanupEngine @Inject constructor() {

    fun clean(rawText: String): CleanedText {
        var text = rawText

        // Rejoin hyphenated line-wraps: "exam-\nple" -> "example"
        text = text.replace(Regex("(\\w)-\\n(\\w)"), "$1$2")

        // Collapse multiple blank lines / spaces
        text = text.replace(Regex("[ \\t]+"), " ")
        text = text.replace(Regex("\\n{3,}"), "\n\n")

        // Fix spacing before punctuation: "word ." -> "word."
        text = text.replace(Regex("\\s+([.,;:!?])"), "$1")

        // Common OCR digit/letter confusions inside otherwise-alphabetic words (e.g. "w0rld" -> heuristically left alone
        // unless surrounded by letters on both sides, to avoid corrupting genuine alphanumeric codes/receipts).
        text = fixDigitLetterConfusion(text)

        return CleanedText(original = rawText, cleaned = text.trim())
    }

    private fun fixDigitLetterConfusion(text: String): String {
        val pattern = Regex("[A-Za-z][01][A-Za-z]+|[A-Za-z]+[01][A-Za-z]")
        return pattern.replace(text) { match ->
            match.value.replace('0', 'o').replace('1', 'l')
        }
    }
}
