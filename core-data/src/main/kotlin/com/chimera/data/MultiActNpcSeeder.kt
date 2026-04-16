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
private data class MultiActNpcJson(
    val id: String,
    val name: String,
    val title: String? = null,
    val role: String,
    val initialDisposition: Float = 0f,
    val archetype: String? = null,
    val portraitResName: String? = null
)

@Singleton
class MultiActNpcSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val actNpcFiles = listOf("npcs.json", "act2_npcs.json", "act3_npcs.json")

    suspend fun seedNpcsForSlot(slotId: Long) {
        // Load all NPCs from all acts, deduplicate by ID (last-writer-wins)
        val allNpcs = actNpcFiles.flatMap { fileName ->
            try {
                val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
                json.decodeFromString<List<MultiActNpcJson>>(text)
            } catch (_: Exception) {
                emptyList()
            }
        }.associateBy { it.id }.values

        val characters = allNpcs.map { npc ->
            CharacterEntity(
                id = npc.id,
                saveSlotId = slotId,
                name = npc.name,
                title = npc.title,
                role = npc.role,
                isPlayerCharacter = false,
                portraitResName = npc.portraitResName
            )
        }
        characterDao.upsertAll(characters)

        allNpcs.forEach { npc ->
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
}
