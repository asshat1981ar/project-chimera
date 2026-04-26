package com.chimera.database.mapper

import com.chimera.database.converter.Converters
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.database.entity.MemoryShardEntity
import com.chimera.database.entity.SaveSlotEntity
import com.chimera.model.Character
import com.chimera.model.CharacterRole
import com.chimera.model.CharacterState
import com.chimera.model.JournalEntry
import com.chimera.model.MemoryShard
import com.chimera.database.entity.QuestEntity
import com.chimera.database.entity.QuestObjectiveEntity
import com.chimera.model.Quest
import com.chimera.model.QuestObjective
import com.chimera.model.QuestObjectiveStatus
import com.chimera.model.QuestObjectiveType
import com.chimera.model.QuestStatus
import com.chimera.model.SaveSlot

private val converters = Converters()

fun SaveSlotEntity.toModel() = SaveSlot(
    id = id,
    slotIndex = slotIndex,
    playerName = playerName,
    chapterTag = chapterTag,
    playtimeSeconds = playtimeSeconds,
    lastPlayedAt = lastPlayedAt,
    createdAt = createdAt,
    isEmpty = isEmpty
)

fun SaveSlot.toEntity() = SaveSlotEntity(
    id = id,
    slotIndex = slotIndex,
    playerName = playerName,
    chapterTag = chapterTag,
    playtimeSeconds = playtimeSeconds,
    lastPlayedAt = lastPlayedAt,
    createdAt = createdAt,
    isEmpty = isEmpty
)

fun CharacterEntity.toModel() = Character(
    id = id,
    saveSlotId = saveSlotId,
    name = name,
    title = title,
    role = try { CharacterRole.valueOf(role) } catch (_: Exception) { CharacterRole.NPC_NEUTRAL },
    isPlayerCharacter = isPlayerCharacter,
    portraitResName = portraitResName
)

fun Character.toEntity() = CharacterEntity(
    id = id,
    saveSlotId = saveSlotId,
    name = name,
    title = title,
    role = role.name,
    isPlayerCharacter = isPlayerCharacter,
    portraitResName = portraitResName
)

fun CharacterStateEntity.toModel() = CharacterState(
    characterId = characterId,
    saveSlotId = saveSlotId,
    healthFraction = healthFraction.coerceIn(0f, 1f),
    dispositionToPlayer = dispositionToPlayer.coerceIn(
        CharacterState.DISPOSITION_MIN, CharacterState.DISPOSITION_MAX
    ),
    emotionalState = converters.toFloatMap(emotionalStateJson),
    activeArchetype = activeArchetype,
    archetypeVariables = converters.toFloatMap(archetypeVariablesJson),
    lastInteractionEpoch = lastInteractionEpoch
)

fun CharacterState.toEntity() = CharacterStateEntity(
    characterId = characterId,
    saveSlotId = saveSlotId,
    healthFraction = healthFraction,
    dispositionToPlayer = dispositionToPlayer,
    emotionalStateJson = converters.fromFloatMap(emotionalState),
    activeArchetype = activeArchetype,
    archetypeVariablesJson = converters.fromFloatMap(archetypeVariables),
    lastInteractionEpoch = lastInteractionEpoch
)

fun MemoryShardEntity.toModel() = MemoryShard(
    id = id,
    saveSlotId = saveSlotId,
    sceneId = sceneId,
    characterId = characterId,
    summary = summary,
    tags = converters.toStringList(tagsJson),
    importanceScore = importanceScore,
    createdAt = createdAt
)

fun JournalEntryEntity.toModel() = JournalEntry(
    id = id,
    saveSlotId = saveSlotId,
    title = title,
    body = body,
    category = category,
    sceneId = sceneId,
    characterId = characterId,
    isRead = isRead,
    createdAt = createdAt
)

fun JournalEntry.toEntity() = JournalEntryEntity(
    id = id,
    saveSlotId = saveSlotId,
    title = title,
    body = body,
    category = category,
    sceneId = sceneId,
    characterId = characterId,
    isRead = isRead,
    createdAt = createdAt
)

fun MemoryShard.toEntity() = MemoryShardEntity(
    id = id,
    saveSlotId = saveSlotId,
    sceneId = sceneId,
    characterId = characterId,
    summary = summary,
    tagsJson = converters.fromStringList(tags),
    importanceScore = importanceScore,
    createdAt = createdAt
)

fun QuestEntity.toModel() = Quest(
    id = id,
    saveSlotId = saveSlotId,
    title = title,
    description = description,
    status = try { QuestStatus.valueOf(status.uppercase()) } catch (_: Exception) { QuestStatus.ACTIVE },
    sourceSceneId = sourceSceneId,
    sourceNpcId = sourceNpcId,
    pinnedOrder = pinnedOrder,
    outcomeText = outcomeText,
    createdAt = createdAt,
    completedAt = completedAt,
    totalSteps = totalSteps,
    currentStep = currentStep
)

fun QuestObjectiveEntity.toModel() = QuestObjective(
    id = id,
    questId = questId,
    stepIndex = stepIndex,
    type = try { QuestObjectiveType.valueOf(type.uppercase().replace(" ", "_")) } catch (_: Exception) { QuestObjectiveType.COMPLETE_SCENE },
    status = try { QuestObjectiveStatus.valueOf(status.uppercase()) } catch (_: Exception) { QuestObjectiveStatus.ACTIVE },
    isRequired = isRequired,
    targetSceneId = targetSceneId,
    targetMapNodeId = targetMapNodeId,
    targetNpcId = targetNpcId,
    targetRumorId = targetRumorId,
    targetRecipeId = targetRecipeId,
    targetItemId = targetItemId,
    title = title,
    storyContext = storyContext,
    recentConsequence = recentConsequence,
    knownRequirement = knownRequirement,
    rewardHint = rewardHint,
    riskHint = riskHint,
    activatedAt = activatedAt,
    completedAt = completedAt
)
