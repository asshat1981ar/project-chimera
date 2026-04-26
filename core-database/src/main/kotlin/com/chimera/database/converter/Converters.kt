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
        } catch (e: Exception) {
            println("WARN: Converters - Malformed JSON for float map: '$value' - ${e.message}")
            emptyMap()
        }

    @TypeConverter
    fun fromStringList(value: List<String>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            println("WARN: Converters - Malformed JSON for string list: '$value' - ${e.message}")
            emptyList()
        }
}