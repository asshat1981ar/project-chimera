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

@Singleton
class NpcSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedNpcsForSlot(slotId: Long) {
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
                portraitResName = npc.portraitResName
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
}
