package com.chimera.ai

import com.chimera.model.Quest
import com.chimera.model.QuestStatus
import com.chimera.model.StoryArc
import com.chimera.model.StoryBeat
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.plugins.timeout
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * Generates procedural storylines and quests using a local Ollama LLM.
 * Provides rich, AI-driven narrative content while keeping data private.
 */
class OllamaStorylineAdapter(
    private val client: io.ktor.client.HttpClient,
    private val baseUrl: String = "http://10.0.2.2:11434",
    private val model: String = "llama3.2"
) : StorylineGenerator {

    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun generateQuestChain(context: QuestGenerationContext): List<Quest> {
        val prompt = buildQuestPrompt(context)
        val response = ollamaGenerate(prompt)
        return parseQuests(response) ?: LocalStorylineGenerator().generateQuestChain(context)
    }

    override suspend fun generateStoryBeat(context: StoryBeatContext): StoryBeat {
        val prompt = buildBeatPrompt(context)
        val response = ollamaGenerate(prompt)
        return parseStoryBeat(response) ?: LocalStorylineGenerator().generateStoryBeat(context)
    }

    override suspend fun isAvailable(): Boolean {
        return try {
            val r = client.get("$baseUrl/api/tags") {
                timeout { requestTimeoutMillis = 5_000 }
            }
            r.status.value in 200..299
        } catch (_: Exception) {
            false
        }
    }

    // ---- internals ----

    private suspend fun ollamaGenerate(prompt: String): String {
        val request = GenerateRequest(
            model = model,
            prompt = prompt,
            stream = false,
            format = "json"
        )

        val httpResponse = client.post("$baseUrl/api/generate") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(json.encodeToString(request))
            timeout { requestTimeoutMillis = 60_000 }
        }

        val body = httpResponse.bodyAsText()
        val gen = json.decodeFromString<GenerateResponse>(body)
        return gen.response
    }

    private fun buildQuestPrompt(ctx: QuestGenerationContext): String {
        return """
            You are a dark fantasy RPG quest designer. Generate 1-2 quests for a player.
            
            Player Level: ${ctx.playerLevel}
            Region Tags: ${ctx.regionTags.joinToString()}
            Active Quests: ${ctx.activeQuestIds.joinToString()}
            Completed Quests: ${ctx.completedQuestIds.joinToString()}
            Recent Events: ${ctx.recentEvents.joinToString()}
            Tone: ${ctx.tone}
            
            Return valid JSON array of quests. Each quest has:
            - title: string
            - description: string  
            - objectives: array of strings
            - rewardType: one of [xp, currency, artifact, ability]
            - rewardAmount: integer
            
            Make quests atmospheric, with moral ambiguity and dark fantasy themes.
            Keep responses under 300 tokens.
        """.trimIndent()
    }

    private fun buildBeatPrompt(ctx: StoryBeatContext): String {
        return """
            You are a narrative designer for a dark fantasy RPG. Generate the next story beat.
            
            Current Arc: ${ctx.currentArc.title} (${ctx.currentArc.tag})
            Recent Choice: ${ctx.recentChoice}
            Player Disposition: ${ctx.playerDisposition.entries.joinToString { "${it.key}=${it.value}" }}
            
            Return valid JSON with:
            - description: atmospheric narrative text (2-3 sentences)
            - type: one of [world_event, plot_hook, character_moment, revelation, combat]
            - consequence: brief description of what happens next
            - choices: array of 2-4 player choices
            
            Keep responses under 200 tokens.
        """.trimIndent()
    }

    private fun parseQuests(raw: String): List<Quest>? {
        return try {
            json.decodeFromString<List<GeneratedQuest>>(raw).map { quest ->
                Quest(
                    id = 0L,
                    saveSlotId = 0L,
                    title = quest.title,
                    description = quest.description,
                    status = QuestStatus.ACTIVE,
                    createdAt = System.currentTimeMillis(),
                    totalSteps = quest.objectives.size,
                    currentStep = 0
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseStoryBeat(raw: String): StoryBeat? {
        return try {
            json.decodeFromString<StoryBeat>(raw)
        } catch (_: Exception) {
            null
        }
    }

    @kotlinx.serialization.Serializable
    private data class GeneratedQuest(
        val title: String,
        val description: String,
        val objectives: List<String> = emptyList(),
        val rewardType: String? = null,
        val rewardAmount: Int = 0
    )

    @kotlinx.serialization.Serializable
    private data class GenerateRequest(
        val model: String,
        val prompt: String,
        val stream: Boolean = false,
        val format: String? = null
    )

    @kotlinx.serialization.Serializable
    private data class GenerateResponse(
        val response: String = ""
    )
}
