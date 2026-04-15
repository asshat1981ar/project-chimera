package com.chimera.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromFloatMap(value: Map<String, Float>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toFloatMap(value: String): Map<String, Float> =
        try {
            json.decodeFromString(value)
        } catch (_: Exception) {
            emptyMap()
        }
}
