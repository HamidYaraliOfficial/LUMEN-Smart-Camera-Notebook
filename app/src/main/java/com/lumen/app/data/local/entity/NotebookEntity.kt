package com.lumen.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notebooks")
data class NotebookEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val icon: String,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isLocked: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
