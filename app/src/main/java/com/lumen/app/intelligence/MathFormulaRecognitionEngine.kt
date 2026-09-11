package com.lumen.app.intelligence

data class FormulaRecognitionResult(
    val rawText: String,
    val bestEffortLatex: String?,
    val confidence: Float,
    val isApproximate: Boolean,
)

/**
 * BETA / roadmap component: simple math formulas are approximated from OCR text using a small
 * symbol-substitution table (e.g. "x^2" -> "x^{2}", "sqrt(" -> "\\sqrt{"). This is a best-effort
 * text transform, not a trained formula-recognition model, and is always labelled
 * [FormulaRecognitionResult.isApproximate] = true with a review prompt in the UI — LUMEN never
 * claims a confident LaTeX reconstruction it cannot actually verify.
 */
class MathFormulaRecognitionEngine {

    private val substitutions = listOf(
        Regex("sqrt\\(([^)]*)\\)") to "\\\\sqrt{$1}",
        Regex("([a-zA-Z0-9])\\^([0-9]+)") to "$1^{$2}",
        Regex("([a-zA-Z0-9])_([0-9]+)") to "$1_{$2}",
        Regex("\\bpi\\b", RegexOption.IGNORE_CASE) to "\\\\pi",
        Regex("<=") to "\\\\leq",
        Regex(">=") to "\\\\geq",
        Regex("!=") to "\\\\neq",
    )

    fun recognize(ocrLine: String): FormulaRecognitionResult {
        val looksLikeFormula = ocrLine.any { it in "=+-*/^_∑∫√πθ" } && ocrLine.any { it.isLetterOrDigit() }
        if (!looksLikeFormula) return FormulaRecognitionResult(ocrLine, null, 0f, true)

        var latex = ocrLine
        for ((pattern, replacement) in substitutions) {
            latex = pattern.replace(latex, replacement)
        }
        return FormulaRecognitionResult(rawText = ocrLine, bestEffortLatex = latex, confidence = 0.4f, isApproximate = true)
    }
}
