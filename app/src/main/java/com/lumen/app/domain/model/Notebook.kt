package com.lumen.app.domain.model

data class Notebook(
    val id: String,
    val name: String,
    val colorHex: String,
    val icon: String,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isLocked: Boolean = false,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val itemCount: Int = 0,
)

/** Built-in starter notebooks created on first launch; user can rename/delete all but keep the idea. */
object DefaultNotebooks {
    val PRESETS = listOf(
        "University" to "#0078D4",
        "Work" to "#1565C0",
        "Personal" to "#2E7D32",
        "Projects" to "#F9A825",
        "Receipts" to "#C62828",
        "Books" to "#6A1B9A",
    )
}
