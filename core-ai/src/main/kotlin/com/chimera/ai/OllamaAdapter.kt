package com.chimera.ai

import android.util.Log
import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.plugins.timeout
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Ollama local LLM adapter.
 * Communicates with a local Ollama instance via HTTP.
 * Default endpoint for Android emulator: http://10.0.2.2:11434 (host loopback).
 *
 * Supports JSON mode for structured dialogue responses when the model supports it.
 * Falls back gracefully to text parsing on JSON mode failure.
 *
 * Recommended models:
 * - llama3.2 (3B, fast, good for dialogue)
 * - mistral (7B, balanced)
 * - dolphin-mixtral (uncensored, good for RPG)
 * - llama3.1 (8B, excellent reasoning)
 */
class OllamaAdapter(
    private val client: HttpClient,
    private val baseUrl: String = "http://10.0.2.2:11434",
    private val model: String = "llama3.2",
    private val timeoutMs: Long = 60_000L,
    private val temperature: Float = 0.7f
) : DialogueProvider {

    /**
     * Whether to request JSON structured output.
     * Enable only if the model supports Ollama's "format": "json" option.
     */
    var useJsonMode: Boolean = true

    @Serializable
    private data class GenerateRequest(
        val model: String,
        val prompt: String,
        val stream: Boolean = false,
        val format: String? = null,
        val options: OllamaOptions? = null
    )

    @Serializable
    private data class OllamaOptions(
        val temperature: Double = 0.7,
        val num_predict: Int = 256,
        val stop: List<String> = emptyList()
    )

    @Serializable
    private data class GenerateResponse(
        val model: String = "",
        val created_at: String = "",
        val response: String = "",
        val done: Boolean = false,
        val context: List<Int> = emptyList()
    )

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
        val fullPrompt = buildSinglePrompt(systemPrompt, userMessage)

        val response = ollamaGenerate(fullPrompt)
        val parsed = DialogueResponseParser.parse(response)
            ?: throw OllamaParseException(
                "Failed to parse Ollama dialogue output: ${response.take(300)}"
            )
        return parsed
    }

    override suspend fun generateIntents(
        contract: SceneContract,
        characterState: CharacterState,
        turnHistory: List<DialogueTurnResult>
    ): List<String> {
        val prompt = PromptAssembler.buildIntentPrompt(contract, characterState, turnHistory)
        val response = ollamaGenerate(prompt)
        return DialogueResponseParser.parseIntents(response) ?: emptyList()
    }

    override suspend fun isAvailable(): Boolean {
        return try {
            val r = client.get("$baseUrl/api/tags") {
                timeout { requestTimeoutMillis = 5_000 }
            }
            r.status.value in 200..299
        } catch (e: Exception) {
            Log.d(TAG, "Ollama not available: ${e.message}")
            false
        }
    }

    /**
     * Fetch list of installed models from Ollama.
     * Returns model names or empty list on failure.
     */
    suspend fun listModels(): List<String> {
        return try {
            val r = client.get("$baseUrl/api/tags") {
                timeout { requestTimeoutMillis = 5_000 }
            }
            val bodyText = r.bodyAsText()
            // Ollama returns: {"models":[{"name":"llama3.2:latest",...},...]}
            val obj = json.parseToJsonElement(bodyText) as kotlinx.serialization.json.JsonObject
            val arr = obj["models"] as? kotlinx.serialization.json.JsonArray ?: return emptyList()
            arr.mapNotNull { (it as? kotlinx.serialization.json.JsonObject)
                ?.get("name")?.toString()?.trim('"') }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to list models: ${e.message}")
            emptyList()
        }
    }

    /** True if Ollama is reachable and the configured model is installed. */
    suspend fun isModelAvailable(): Boolean {
        if (!isAvailable()) return false
        return try {
            listModels().any { it.startsWith(model) }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Call Ollama /api/generate and return the response text.
     */
    private suspend fun ollamaGenerate(prompt: String): String {
        val request = GenerateRequest(
            model = model,
            prompt = prompt,
            stream = false,
            format = if (useJsonMode) "json" else null,
            options = OllamaOptions(
                temperature = temperature.toDouble(),
                num_predict = 256,
                stop = listOf("### USER", "Player says:", "\n\n\n")
            )
        )

        val url = "$baseUrl/api/generate"
        val httpResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request))
            timeout { requestTimeoutMillis = timeoutMs }
        }

        val body = httpResponse.bodyAsText()
        if (httpResponse.status.value !in 200..299) {
            throw OllamaApiException("Ollama returned HTTP ${httpResponse.status.value}: ${body.take(500)}")
        }

        val generateResponse = json.decodeFromString<GenerateResponse>(body)
        return generateResponse.response
    }

    /**
     * Build a single-text prompt from system + user messages.
     * Uses a simple instruct format that works well with open-weight models.
     */
    private fun buildSinglePrompt(system: String, user: String): String {
        return buildString {
            appendLine("### INSTRUCTION")
            appendLine(system)
            appendLine()
            appendLine("### INPUT")
            appendLine(user)
            appendLine()
            appendLine("### RESPONSE (valid JSON only)")
        }
    }

    companion object {
        private const val TAG = "OllamaAdapter"

        /**
         * Check if a local Ollama instance is running.
         * @param baseUrl Ollama host. For emulator, use 10.0.2.2:11434 to reach host loopback.
         */
        suspend fun probe(client: HttpClient, baseUrl: String = "http://10.0.2.2:11434"): Boolean {
            return try {
                val r = client.get("$baseUrl/api/tags") {
                    timeout { requestTimeoutMillis = 3_000 }
                }
                r.status.value in 200..299
            } catch (_: Exception) {
                false
            }
        }
    }
}

class OllamaApiException(message: String) : Exception(message)
class OllamaParseException(message: String) : Exception(message)
