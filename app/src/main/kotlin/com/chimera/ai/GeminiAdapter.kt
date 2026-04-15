package com.chimera.ai

import android.util.Log
import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Google AI Studio (Gemini) adapter.
 * Free tier: 1,500 req/day, Gemini 2.5 Flash.
 * Endpoint: https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
 */
class GeminiAdapter(
    private val client: HttpClient,
    private val apiKey: String,
    private val model: String = "gemini-2.0-flash-lite"
) : DialogueProvider {

    @Serializable
    private data class GeminiRequest(
        val contents: List<Content>,
        val systemInstruction: SystemInstruction? = null
    )

    @Serializable
    private data class SystemInstruction(val parts: List<Part>)

    @Serializable
    private data class Content(val parts: List<Part>, val role: String = "user")

    @Serializable
    private data class Part(val text: String)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun generateTurn(
        contract: SceneContract,
        playerInput: PlayerInput,
        characterState: CharacterState,
        recentMemories: List<MemoryShard>,
        turnHistory: List<DialogueTurnResult>
    ): DialogueTurnResult {
        val systemPrompt = PromptAssembler.buildSystemPrompt(contract, characterState)
        val userMessage = PromptAssembler.buildUserMessage(playerInput, recentMemories, turnHistory)

        val request = GeminiRequest(
            systemInstruction = SystemInstruction(parts = listOf(Part(systemPrompt))),
            contents = listOf(Content(parts = listOf(Part(userMessage))))
        )

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }

        val body = response.bodyAsText()
        val text = extractGeminiText(body) ?: throw Exception("Empty Gemini response")
        return DialogueResponseParser.parse(text) ?: throw Exception("Failed to parse Gemini output")
    }

    override suspend fun generateIntents(
        contract: SceneContract,
        characterState: CharacterState,
        turnHistory: List<DialogueTurnResult>
    ): List<String> {
        val prompt = PromptAssembler.buildIntentPrompt(contract, characterState, turnHistory)
        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(prompt))))
        )

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }

        val body = response.bodyAsText()
        val text = extractGeminiText(body) ?: return emptyList()
        return DialogueResponseParser.parseIntents(text) ?: emptyList()
    }

    override suspend fun isAvailable(): Boolean {
        return apiKey.isNotBlank()
    }

    private fun extractGeminiText(body: String): String? {
        return try {
            // Gemini response: {"candidates":[{"content":{"parts":[{"text":"..."}]}}]}
            val obj = json.parseToJsonElement(body)
            val candidates = obj.jsonObject["candidates"]?.jsonArray ?: return null
            val content = candidates[0].jsonObject["content"]?.jsonObject ?: return null
            val parts = content["parts"]?.jsonArray ?: return null
            parts[0].jsonObject["text"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            Log.e("GeminiAdapter", "Failed to extract text from response", e)
            null
        }
    }

    private val kotlinx.serialization.json.JsonElement.jsonObject get() =
        this as kotlinx.serialization.json.JsonObject
    private val kotlinx.serialization.json.JsonElement.jsonArray get() =
        (this as kotlinx.serialization.json.JsonArray)
    private val kotlinx.serialization.json.JsonElement.jsonPrimitive get() =
        (this as kotlinx.serialization.json.JsonPrimitive)
}
