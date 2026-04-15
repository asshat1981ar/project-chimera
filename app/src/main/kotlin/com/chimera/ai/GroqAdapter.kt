package com.chimera.ai

import android.util.Log
import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Groq adapter using OpenAI-compatible chat completions API.
 * Free tier: 30 RPM, 14.4K tokens/min with Llama models.
 */
class GroqAdapter(
    private val client: HttpClient,
    private val apiKey: String,
    private val model: String = "llama-3.3-70b-versatile"
) : DialogueProvider {

    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>,
        val temperature: Float = 0.7f,
        val max_tokens: Int = 300
    )

    @Serializable
    private data class ChatMessage(val role: String, val content: String)

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

        val request = ChatRequest(
            model = model,
            messages = listOf(
                ChatMessage("system", systemPrompt),
                ChatMessage("user", userMessage)
            )
        )

        val response = client.post("https://api.groq.com/openai/v1/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }

        val body = response.bodyAsText()
        val text = extractChatText(body) ?: throw Exception("Empty Groq response")
        return DialogueResponseParser.parse(text) ?: throw Exception("Failed to parse Groq output")
    }

    override suspend fun generateIntents(
        contract: SceneContract,
        characterState: CharacterState,
        turnHistory: List<DialogueTurnResult>
    ): List<String> {
        val prompt = PromptAssembler.buildIntentPrompt(contract, characterState, turnHistory)
        val request = ChatRequest(
            model = model,
            messages = listOf(ChatMessage("user", prompt)),
            max_tokens = 150
        )

        val response = client.post("https://api.groq.com/openai/v1/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }

        val body = response.bodyAsText()
        val text = extractChatText(body) ?: return emptyList()
        return DialogueResponseParser.parseIntents(text) ?: emptyList()
    }

    override suspend fun isAvailable(): Boolean = apiKey.isNotBlank()

    private fun extractChatText(body: String): String? {
        return try {
            val obj = json.parseToJsonElement(body)
            val choices = (obj as kotlinx.serialization.json.JsonObject)["choices"]
                as? kotlinx.serialization.json.JsonArray ?: return null
            val message = (choices[0] as kotlinx.serialization.json.JsonObject)["message"]
                as? kotlinx.serialization.json.JsonObject ?: return null
            (message["content"] as? kotlinx.serialization.json.JsonPrimitive)?.content
        } catch (e: Exception) {
            Log.e("GroqAdapter", "Failed to extract chat text", e)
            null
        }
    }
}
