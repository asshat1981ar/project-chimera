package com.chimera.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Calls the HuggingFace Inference API to generate a character portrait image.
 *
 * Model: stabilityai/stable-diffusion-xl-base-1.0 (free tier, widely available)
 * Returns raw JPEG bytes, or null on any failure — callers degrade gracefully.
 *
 * Adversarial notes:
 * - 503 with estimated_time: model is loading; WorkManager retries handle this
 * - Empty hfToken: returns null immediately (no wasted quota)
 * - Rate limit (429): exponential backoff via HttpRequestRetry
 */
class PortraitGenerationService(
    private val hfToken: String,
    private val modelId: String = DEFAULT_MODEL
) {
    private val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(HttpRequestRetry) {
            maxRetries = 2
            retryOnServerErrors()
            exponentialDelay(base = 2.0, initialDelayMs = 3_000L, maxDelayMs = 15_000L)
        }
        engine {
            connectTimeout = 20_000
            socketTimeout  = 60_000   // image generation takes time
        }
    }

    /**
     * Generates a portrait for the given NPC.
     * @return JPEG bytes, or null if token is empty / API fails / model unavailable.
     */
    suspend fun generatePortrait(
        npcName: String,
        npcRole: String,
        npcTitle: String?
    ): ByteArray? {
        if (hfToken.isBlank()) return null

        val prompt = buildPrompt(npcName, npcRole, npcTitle)
        return try {
            val response = client.post(
                "https://api-inference.huggingface.co/models/$modelId"
            ) {
                header(HttpHeaders.Authorization, "Bearer $hfToken")
                contentType(ContentType.Application.Json)
                setBody("""{"inputs":"$prompt","parameters":{"num_inference_steps":20}}""")
            }
            when {
                response.status.isSuccess()             -> response.body<ByteArray>()
                response.status == HttpStatusCode(503, "Service Unavailable") -> null // model loading, WM retries
                else                                    -> null
            }
        } catch (e: Exception) {
            null   // network error, disk full, etc. — never crash; WM will retry
        }
    }

    fun close() = client.close()

    private fun buildPrompt(name: String, role: String, title: String?): String {
        val roleHint = when (role.uppercase()) {
            "COMPANION"  -> "trusted companion, loyal ally"
            "ANTAGONIST" -> "menacing villain, threatening"
            "MENTOR"     -> "wise elder, experienced guide"
            "MERCHANT"   -> "traveling merchant, weathered"
            "GUARDIAN"   -> "armored guardian, stoic warrior"
            else         -> "fantasy NPC"
        }
        val titlePart = if (!title.isNullOrBlank()) "$title, " else ""
        return "detailed fantasy RPG portrait of $name, ${titlePart}$roleHint, " +
               "dark fantasy art, dramatic lighting, highly detailed face, " +
               "character portrait, professional concept art"
    }

    companion object {
        const val DEFAULT_MODEL = "stabilityai/stable-diffusion-xl-base-1.0"
    }
}
