package com.chimera.model

import kotlinx.serialization.Serializable

/**
 * Contract for a single NPC dialogue turn produced by any provider (AI or fallback).
 */
@Serializable
data class DialogueTurnResult(
    val npcLine: String,
    val emotion: String = "neutral",
    val relationshipDelta: Float = 0f,
    val flags: List<String> = emptyList(),
    val memoryCandidates: List<String> = emptyList(),
    val directorNotes: String = ""
)

/**
 * Scene contract that constrains what the AI provider can generate.
 */
@Serializable
data class SceneContract(
    val sceneId: String,
    val sceneTitle: String,
    val npcId: String,
    val npcName: String,
    val setting: String = "",
    val stakes: String = "",
    val allowedReveals: List<String> = emptyList(),
    val forbiddenTopics: List<String> = emptyList(),
    val maxTurns: Int = 12
)

/**
 * Player input for a dialogue turn, combining typed text or selected intent.
 */
@Serializable
data class PlayerInput(
    val text: String,
    val isQuickIntent: Boolean = false,
    val intentIndex: Int = -1
)

/**
 * Memory shard: a compact canonical summary of a dialogue moment.
 */
@Serializable
data class MemoryShard(
    val id: Long = 0,
    val saveSlotId: Long,
    val sceneId: String,
    val characterId: String,
    val summary: String,
    val tags: List<String> = emptyList(),
    val importanceScore: Float = 0.5f,
    val createdAt: Long = System.currentTimeMillis()
)
