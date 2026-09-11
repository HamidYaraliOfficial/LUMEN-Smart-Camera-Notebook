package com.lumen.app.data.local.entity

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Room type converters for simple list/JSON fields. Kept dependency-light (kotlinx.serialization). */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>?): String = json.encodeToString(value ?: emptyList())

    @TypeConverter
    fun toStringList(value: String): List<String> =
        runCatching { json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())
}
