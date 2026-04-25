package com.chimera.data

import android.content.Context
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class NpcJson(
    val id: String,
    val name: String,
    val title: String? = null,
    val role: String,
    val initialDisposition: Float = 0f,
    val archetype: String? = null,
    val portraitResName: String? = null
)

@Serializable
private data class PortraitManifest(
    val portraits: List<PortraitEntry>
)

@Serializable
private data class PortraitEntry(
    val characterId: String,
    val drawableName: String
)

@Singleton
class NpcSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Seeds NPCs for a given save slot, populating portraitResName from the portrait manifest.
     * The manifest maps character IDs to drawable resource names.
     */
    suspend fun seedNpcsForSlot(slotId: Long) {
        // Load portrait manifest
        val portraitMap = loadPortraitManifest()

        // Load NPCs and populate portrait data
        val text = context.assets.open("npcs.json").bufferedReader().use { it.readText() }
        val npcs = json.decodeFromString<List<NpcJson>>(text)

        val characters = npcs.map { npc ->
            CharacterEntity(
                id = npc.id,
                saveSlotId = slotId,
                name = npc.name,
                title = npc.title,
                role = npc.role,
                isPlayerCharacter = false,
                portraitResName = portraitMap[npc.id] ?: npc.portraitResName
            )
        }
        characterDao.upsertAll(characters)

        npcs.forEach { npc ->
            characterStateDao.upsert(
                CharacterStateEntity(
                    characterId = npc.id,
                    saveSlotId = slotId,
                    dispositionToPlayer = npc.initialDisposition,
                    activeArchetype = npc.archetype
                )
            )
        }
    }

    /**
     * Loads the portrait manifest from assets and returns a map of characterId -> drawableName.
     */
    private fun loadPortraitManifest(): Map<String, String> {
        return try {
            val manifestText = context.assets.open("portrait_manifest.json")
                .bufferedReader()
                .use { it.readText() }
            val manifest = json.decodeFromString<PortraitManifest>(manifestText)
            manifest.portraits.associateBy({ it.characterId }, { it.drawableName })
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
