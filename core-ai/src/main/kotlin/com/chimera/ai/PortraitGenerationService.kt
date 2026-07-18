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
 * Model: black-forest-labs/FLUX.1-schnell (Apache-2.0, distilled for 1-4 step
 * inference, live on HF's hf-inference provider). Returns raw JPEG bytes, or
 * null on any failure — callers degrade gracefully.
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
     * @param equipmentDescriptor Optional gear description (e.g. "a weathered leather
     *   cloak") appended to the prompt. Null renders the character's base likeness.
     * @return JPEG bytes, or null if token is empty / API fails / model unavailable.
     */
    suspend fun generatePortrait(
        npcName: String,
        npcRole: String,
        npcTitle: String?,
        equipmentDescriptor: String? = null
    ): ByteArray? = requestImage(buildPortraitPrompt(npcName, npcRole, npcTitle, equipmentDescriptor))

    /**
     * Generates a small overhead map-token variant for the given NPC, for use
     * as a movable marker on the world map (distinct framing from the portrait).
     * @param equipmentDescriptor Optional gear description, see [generatePortrait].
     * @return JPEG bytes, or null if token is empty / API fails / model unavailable.
     */
    suspend fun generateMapToken(
        npcName: String,
        npcRole: String,
        npcTitle: String?,
        equipmentDescriptor: String? = null
    ): ByteArray? = requestImage(buildTokenPrompt(npcName, npcRole, npcTitle, equipmentDescriptor))

    fun close() = client.close()

    private suspend fun requestImage(prompt: String): ByteArray? {
        if (hfToken.isBlank()) return null

        return try {
            val response = client.post(
                "https://api-inference.huggingface.co/models/$modelId"
            ) {
                header(HttpHeaders.Authorization, "Bearer $hfToken")
                contentType(ContentType.Application.Json)
                setBody("""{"inputs":"$prompt","parameters":{"num_inference_steps":4}}""")
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

    companion object {
        const val DEFAULT_MODEL = "black-forest-labs/FLUX.1-schnell"
    }
}

// Pure prompt-building logic, kept free of PortraitGenerationService's HttpClient
// construction so it can be unit-tested directly without an Android/Ktor engine.

internal fun roleHint(role: String): String = when (role.uppercase()) {
    "COMPANION"  -> "trusted companion, loyal ally"
    "ANTAGONIST" -> "menacing villain, threatening"
    "MENTOR"     -> "wise elder, experienced guide"
    "MERCHANT"   -> "traveling merchant, weathered"
    "GUARDIAN"   -> "armored guardian, stoic warrior"
    "PROTAGONIST" -> "the protagonist, weathered traveler, resolute"
    else         -> "fantasy NPC"
}

internal fun buildPortraitPrompt(
    name: String,
    role: String,
    title: String?,
    equipmentDescriptor: String? = null
): String {
    val titlePart = if (!title.isNullOrBlank()) "$title, " else ""
    val equipmentPart = if (!equipmentDescriptor.isNullOrBlank()) "wearing $equipmentDescriptor, " else ""
    return "Dark gothic manuscript portrait of $name, ${titlePart}${roleHint(role)}, ${equipmentPart}" +
           "sumi-e ink wash on aged parchment, visible brush strokes, expressive brushwork, " +
           "ink bleed edges, monochrome charcoal with restrained accent color, " +
           "single candlelight from upper left, deep shadows, parchment grain texture, " +
           "no clean lines, no text, no watermark, isolated portrait, centered upper body, " +
           "character portrait, game asset sprite"
}

internal fun buildTokenPrompt(
    name: String,
    role: String,
    title: String?,
    equipmentDescriptor: String? = null
): String {
    val titlePart = if (!title.isNullOrBlank()) "$title, " else ""
    val equipmentPart = if (!equipmentDescriptor.isNullOrBlank()) "wearing $equipmentDescriptor, " else ""
    return "Dark gothic ink wash overhead map token of $name, ${titlePart}${roleHint(role)}, ${equipmentPart}" +
           "small birds-eye view silhouette, top-down perspective, sumi-e ink on aged parchment, " +
           "visible brush strokes, monochrome charcoal with restrained accent color, " +
           "centered, compact composition, no clean lines, no text, no watermark, " +
           "game map marker sprite"
}

/**
 * Stable cache-key hash for an equipped loadout (order-independent — sorted item IDs).
 * "base" is a reserved sentinel meaning no equipment (the character's base likeness),
 * kept distinct from any real hash so it never collides. Public (unlike the prompt
 * builders above) because callers outside core-ai need it to compute cache file paths.
 */
fun loadoutHash(equippedItemIds: List<String>): String {
    if (equippedItemIds.isEmpty()) return "base"
    return equippedItemIds.sorted().joinToString(",").hashCode().toUInt().toString(16)
}
