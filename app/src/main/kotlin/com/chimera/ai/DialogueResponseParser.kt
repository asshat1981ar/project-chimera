package com.chimera.ai

import android.util.Log
import com.chimera.model.DialogueTurnResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parses and validates AI provider responses into DialogueTurnResult.
 * Handles malformed JSON, missing fields, and out-of-range values.
 */
object DialogueResponseParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Parse a raw AI response string into a DialogueTurnResult.
     * Returns null if parsing fails completely.
     */
    fun parse(raw: String): DialogueTurnResult? {
        return try {
            // Try direct deserialization first
            json.decodeFromString<DialogueTurnResult>(extractJson(raw))
        } catch (e: Exception) {
            Log.w("DialogueParser", "Structured parse failed, trying manual extraction", e)
            tryManualParse(raw)
        }
    }

    /**
     * Parse an intent list response. Returns null on failure.
     */
    fun parseIntents(raw: String): List<String>? {
        return try {
            val jsonStr = extractJson(raw)
            val array = json.parseToJsonElement(jsonStr).jsonArray
            array.map { it.jsonPrimitive.content }.filter { it.isNotBlank() }.take(5)
        } catch (e: Exception) {
            Log.w("DialogueParser", "Intent parse failed", e)
            null
        }
    }

    /**
     * Extract JSON from a response that may contain markdown fences or preamble text.
     */
    private fun extractJson(raw: String): String {
        val trimmed = raw.trim()
        // Strip markdown code fences
        val fencePattern = Regex("```(?:json)?\\s*\\n?(.*?)\\n?```", RegexOption.DOT_MATCHES_ALL)
        val fenceMatch = fencePattern.find(trimmed)
        if (fenceMatch != null) return fenceMatch.groupValues[1].trim()

        // Find first { or [ and match to closing
        val start = trimmed.indexOfFirst { it == '{' || it == '[' }
        if (start == -1) return trimmed
        val end = if (trimmed[start] == '{') trimmed.lastIndexOf('}') else trimmed.lastIndexOf(']')
        if (end == -1 || end <= start) return trimmed
        return trimmed.substring(start, end + 1)
    }

    /**
     * Manual field extraction when structured parse fails.
     */
    private fun tryManualParse(raw: String): DialogueTurnResult? {
        return try {
            val jsonStr = extractJson(raw)
            val obj = json.parseToJsonElement(jsonStr) as? JsonObject ?: return null

            val npcLine = obj["npcLine"]?.jsonPrimitive?.content
                ?: obj["npc_line"]?.jsonPrimitive?.content
                ?: obj["line"]?.jsonPrimitive?.content
                ?: obj["response"]?.jsonPrimitive?.content
                ?: return null

            val emotion = obj["emotion"]?.jsonPrimitive?.content ?: "neutral"
            val delta = try {
                obj["relationshipDelta"]?.jsonPrimitive?.float ?: 0f
            } catch (_: Exception) { 0f }

            val flags = try {
                obj["flags"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            } catch (_: Exception) { emptyList() }

            val memories = try {
                obj["memoryCandidates"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            } catch (_: Exception) { emptyList() }

            DialogueTurnResult(
                npcLine = npcLine,
                emotion = emotion,
                relationshipDelta = delta.coerceIn(-0.25f, 0.25f),
                flags = flags,
                memoryCandidates = memories.take(3)
            )
        } catch (e: Exception) {
            Log.e("DialogueParser", "Manual parse also failed", e)
            null
        }
    }
}
