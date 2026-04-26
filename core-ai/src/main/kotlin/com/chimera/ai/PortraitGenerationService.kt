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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Calls the HuggingFace Inference API to generate a character portrait image.
 *
 * Model: black-forest-labs/FLUX.1-schnell (HF router, text-to-image)
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
            exponentialDelay(base = 2.0, maxDelayMs = 15_000L)
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
        npcTitle: String?,
        identityKey: String = npcName,
        mood: String = "neutral",
        status: String = "steady",
        disposition: Float = 0f,
        archetype: String? = null,
        healthFraction: Float = 1f
    ): ByteArray? {
        if (hfToken.isBlank()) return null

        val prompt = buildPrompt(
            name = npcName,
            role = npcRole,
            title = npcTitle,
            identityKey = identityKey,
            mood = mood,
            status = status,
            disposition = disposition,
            archetype = archetype,
            healthFraction = healthFraction
        )
        return try {
            val response = client.post(
                "https://router.huggingface.co/hf-inference/models/$modelId"
            ) {
                header(HttpHeaders.Authorization, "Bearer $hfToken")
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("inputs", prompt)
                        put("parameters", buildJsonObject {
                            put("num_inference_steps", 20)
                            put("seed", stableSeed(identityKey))
                        })
                    }.toString()
                )
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

    private fun buildPrompt(
        name: String,
        role: String,
        title: String?,
        identityKey: String,
        mood: String,
        status: String,
        disposition: Float,
        archetype: String?,
        healthFraction: Float
    ): String {
        val roleHint = when (role.uppercase()) {
            "COMPANION", "NPC_ALLY" -> "trusted companion, loyal ally"
            "ANTAGONIST", "NPC_HOSTILE", "FACTION_LEADER" -> "menacing villain, threatening presence"
            "MENTOR" -> "wise elder, experienced guide"
            "MERCHANT" -> "traveling merchant, weathered"
            "GUARDIAN" -> "armored guardian, stoic warrior"
            else -> "fantasy NPC"
        }
        val titlePart = if (!title.isNullOrBlank()) "$title, " else ""
        val dispositionHint = when {
            disposition > 0.45f -> "trusting expression toward the viewer"
            disposition > 0.15f -> "cautiously warm expression"
            disposition < -0.45f -> "hostile expression toward the viewer"
            disposition < -0.15f -> "guarded suspicious expression"
            else -> "measured neutral expression"
        }
        val healthHint = when {
            healthFraction < 0.35f -> "wounded and exhausted"
            healthFraction < 0.7f -> "fatigued but standing"
            else -> "steady and alert"
        }
        val archetypeHint = archetype
            ?.replace('_', ' ')
            ?.lowercase()
            ?.let { ", behavioral theme: $it" }
            .orEmpty()
        val identityHint = "stable identity anchor: Project Chimera NPC $identityKey, " +
            "same face, same age, same facial structure, same costume silhouette"
        return "detailed fantasy RPG portrait of $name, ${titlePart}$roleHint, " +
               "$identityHint, " +
               "current mood: $mood, current status: $status, $dispositionHint, $healthHint$archetypeHint, " +
               "dark fantasy art, dramatic lighting, highly detailed face, " +
               "character portrait, professional concept art"
    }

    private fun stableSeed(identityKey: String): Int =
        identityKey.hashCode().and(Int.MAX_VALUE).coerceAtLeast(1)

    companion object {
        const val DEFAULT_MODEL = "black-forest-labs/FLUX.1-schnell"
    }
}
