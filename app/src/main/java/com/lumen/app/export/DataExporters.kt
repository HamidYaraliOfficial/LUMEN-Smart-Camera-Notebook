package com.lumen.app.export

import com.lumen.app.domain.model.LumenDataTable
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor() {
    fun export(table: LumenDataTable, outputFile: File): File {
        val builder = StringBuilder()
        builder.appendLine(table.columns.sortedBy { it.orderIndex }.joinToString(",") { csvEscape(it.name) })
        for (row in table.rows.sortedBy { it.orderIndex }) {
            val orderedCells = table.columns.sortedBy { it.orderIndex }.map { col -> row.cells.firstOrNull { it.columnId == col.id }?.value.orEmpty() }
            builder.appendLine(orderedCells.joinToString(",") { csvEscape(it) })
        }
        outputFile.writeText(builder.toString())
        return outputFile
    }

    private fun csvEscape(value: String): String =
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
}

@Singleton
class JsonExporter @Inject constructor() {
    fun exportTable(table: LumenDataTable, outputFile: File): File {
        val json = kotlinx.serialization.json.Json { prettyPrint = true }
        val obj = buildString {
            append("{\n")
            append("  \"title\": ${jsonString(table.title)},\n")
            append("  \"columns\": [${table.columns.sortedBy { it.orderIndex }.joinToString(",") { jsonString(it.name) }}],\n")
            append("  \"rows\": [\n")
            val orderedCols = table.columns.sortedBy { it.orderIndex }
            table.rows.sortedBy { it.orderIndex }.forEachIndexed { i, row ->
                val values = orderedCols.map { col -> row.cells.firstOrNull { it.columnId == col.id }?.value.orEmpty() }
                append("    [${values.joinToString(",") { jsonString(it) }}]")
                if (i != table.rows.lastIndex) append(",")
                append("\n")
            }
            append("  ]\n}")
        }
        outputFile.writeText(obj)
        return outputFile
    }

    private fun jsonString(s: String) = "\"${s.replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

@Singleton
class MarkdownExporter @Inject constructor() {
    fun exportNote(title: String, bodyMarkdown: String, tags: List<String>, outputFile: File): File {
        val content = buildString {
            append("# $title\n\n")
            if (tags.isNotEmpty()) append(tags.joinToString(" ") { "#$it" } + "\n\n")
            append(bodyMarkdown)
        }
        outputFile.writeText(content)
        return outputFile
    }

    fun exportTable(table: LumenDataTable, outputFile: File): File {
        val cols = table.columns.sortedBy { it.orderIndex }
        val content = buildString {
            append("| ${cols.joinToString(" | ") { it.name }} |\n")
            append("| ${cols.joinToString(" | ") { "---" }} |\n")
            for (row in table.rows.sortedBy { it.orderIndex }) {
                val values = cols.map { col -> row.cells.firstOrNull { it.columnId == col.id }?.value.orEmpty() }
                append("| ${values.joinToString(" | ")} |\n")
            }
        }
        outputFile.writeText(content)
        return outputFile
    }
}

@Singleton
class TxtExporter @Inject constructor() {
    fun export(text: String, outputFile: File): File {
        outputFile.writeText(text)
        return outputFile
    }
}
