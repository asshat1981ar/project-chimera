package com.chimera.database.converter

import android.util.Log
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
            Log.w("Converters", "Malformed JSON for float map: '$value'", e)
            emptyMap()
        }
}
