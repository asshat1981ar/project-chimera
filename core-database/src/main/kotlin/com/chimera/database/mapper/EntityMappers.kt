package com.chimera.database.mapper

import com.chimera.database.converter.Converters
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import com.chimera.database.entity.SaveSlotEntity
import com.chimera.model.Character
import com.chimera.model.CharacterRole
import com.chimera.model.CharacterState
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
    healthFraction = healthFraction,
    dispositionToPlayer = dispositionToPlayer,
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
