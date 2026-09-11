package com.lumen.app.intelligence

import com.lumen.app.domain.model.LumenEvent
import com.lumen.app.domain.model.LumenTask
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Real regex-based date/deadline/action-item extractor used by the Calendar/Task Integration Layer. */
@Singleton
class DateTimeExtractionEngine @Inject constructor() {

    private val dateFormats = listOf(
        "yyyy-MM-dd", "yyyy/MM/dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy",
    )
    private val dateRegex = Regex("\\b(\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}|\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b")
    private val actionVerbs = listOf(
        "submit", "send", "call", "pay", "review", "finish", "complete", "meet", "deliver",
        "ارسال", "تماس", "پرداخت", "بررسی", "تحویل", "جلسه",
        "提交", "发送", "致电", "支付", "审核", "完成", "交付", "会议",
    )

    fun extractTasks(sourceDocumentId: String, fullText: String): List<LumenTask> {
        val tasks = mutableListOf<LumenTask>()
        for (rawLine in fullText.lines()) {
            val line = rawLine.trim()
            if (line.isBlank()) continue
            val hasVerb = actionVerbs.any { line.contains(it, ignoreCase = true) }
            val isBullet = line.startsWith("-") || line.startsWith("•") || line.startsWith("*") || line.matches(Regex("^\\d+[.)].*"))
            if (hasVerb || isBullet) {
                val due = parseFirstDate(line)
                tasks += LumenTask(
                    id = UUID.randomUUID().toString(),
                    title = line.trimStart('-', '•', '*', ' '),
                    notes = null,
                    dueAtEpochMs = due,
                    sourceDocumentId = sourceDocumentId,
                    sourceBlockId = null,
                    createdAtEpochMs = System.currentTimeMillis(),
                )
            }
        }
        return tasks
    }

    fun extractEvents(sourceDocumentId: String, fullText: String): List<LumenEvent> {
        val events = mutableListOf<LumenEvent>()
        for (rawLine in fullText.lines()) {
            val line = rawLine.trim()
            val due = parseFirstDate(line) ?: continue
            val looksLikeEvent = listOf("meeting", "event", "جلسه", "رویداد", "会议", "活动").any {
                line.contains(it, ignoreCase = true)
            }
            if (looksLikeEvent) {
                events += LumenEvent(
                    id = UUID.randomUUID().toString(),
                    title = line,
                    startAtEpochMs = due,
                    endAtEpochMs = null,
                    location = null,
                    sourceDocumentId = sourceDocumentId,
                )
            }
        }
        return events
    }

    private fun parseFirstDate(text: String): Long? {
        val match = dateRegex.find(text) ?: return null
        for (pattern in dateFormats) {
            val sdf = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
            runCatching {
                val date = sdf.parse(match.value)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    return cal.timeInMillis
                }
            }
        }
        return null
    }
}
