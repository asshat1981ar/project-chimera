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

/**
 * Multi-act NPC seeder.
 *
 * The original [NpcSeeder] only reads `npcs.json` (Act 1 NPCs). This class
 * additionally seeds NPCs from `act2_npcs.json` and `act3_npcs.json` so that
 * Kael, Seren, Dara, Rook, and the Living Corruption are available from the
 * database on new game creation.
 *
 * De-duplication is handled by Room's [OnConflictStrategy.REPLACE] strategy —
 * re-seeding an existing slot is safe and idempotent.
 *
 * Inject this in place of (or alongside) [NpcSeeder]. The original [NpcSeeder]
 * is preserved for backward compatibility.
 */
@Singleton
class MultiActNpcSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** NPC source files in load order. Act 1 listed last so it wins on conflict. */
    private val npcFiles = listOf(
        "act2_npcs.json",
        "act3_npcs.json",
        "npcs.json"    // act1 — overwrites any duplicate IDs from act2/3
    )

    /**
     * Seeds all NPCs across all acts for the given [slotId].
     * Idempotent — safe to call on an existing slot.
     */
    suspend fun seedNpcsForSlot(slotId: Long) {
        val allNpcs = npcFiles.flatMap { parseFile(it) }

        // De-duplicate by ID — last writer wins (acts 2 & 3 first, act 1 last
        // ensures act-1 definitions take precedence for shared NPCs like Thorne).
        val deduped = allNpcs.associateBy { it.id }.values.toList()

        val characters = deduped.map { npc ->
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

        deduped.forEach { npc ->
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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun parseFile(filename: String): List<NpcJson> =
        try {
            val text = context.assets.open(filename).bufferedReader().use { it.readText() }
            json.decodeFromString<List<NpcJson>>(text)
        } catch (e: Exception) {
            emptyList()
        }

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
}
