package com.chimera.data

import android.content.Context
import android.util.Log
import com.chimera.core.engine.RelationshipArchetypeEngine
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
 * Multi-act NPC seeder — the canonical seeder for new game creation.
 *
 * Seeds NPCs from `npcs.json` (Act 1), `act2_npcs.json`, and `act3_npcs.json`
 * so that Kael, Seren, Dara, Rook, and the Living Corruption are available
 * from the database on new game creation. Each NPC's `portraitResName` is
 * populated from `portrait_manifest.json` (characterId → drawable name),
 * falling back to the value in the NPC JSON when unmapped.
 *
 * De-duplication is handled by Room's [OnConflictStrategy.REPLACE] strategy —
 * re-seeding an existing slot is safe and idempotent.
 */
@Singleton
class MultiActNpcSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val archetypeEngine: RelationshipArchetypeEngine
) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val TAG = "MultiActNpcSeeder"
        private const val DEFAULT_PLAYER_ID = "player"
    }

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
        val portraitMap = loadPortraitManifest()
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
                portraitResName = portraitMap[npc.id] ?: npc.portraitResName
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
            npc.archetype?.let { initializeArchetypeFor(npc.id, it) }
        }
    }

    /**
     * Starts the live [RelationshipArchetypeEngine] simulation for an NPC whose static
     * archetype label was seeded above. Safe to call repeatedly (e.g. re-seeding an
     * existing slot) -- initializeArchetype just replaces the prior in-memory instance.
     */
    private suspend fun initializeArchetypeFor(npcId: String, archetypeLabel: String) {
        val type = try {
            RelationshipArchetypeEngine.ArchetypeType.valueOf(archetypeLabel)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Unknown archetype label '$archetypeLabel' for NPC $npcId -- skipping", e)
            return
        }
        archetypeEngine.initializeArchetype(type, npcId, DEFAULT_PLAYER_ID)
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

    /**
     * Loads the portrait manifest from assets and returns a map of characterId -> drawableName.
     * Missing or malformed manifest degrades to an empty map (letter-avatar fallback in UI).
     */
    private fun loadPortraitManifest(): Map<String, String> =
        try {
            val manifestText = context.assets.open("portrait_manifest.json")
                .bufferedReader()
                .use { it.readText() }
            val manifest = json.decodeFromString<PortraitManifest>(manifestText)
            manifest.portraits.associateBy({ it.characterId }, { it.drawableName })
        } catch (e: Exception) {
            emptyMap()
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

    @Serializable
    private data class PortraitManifest(
        val portraits: List<PortraitEntry>
    )

    @Serializable
    private data class PortraitEntry(
        val characterId: String,
        val drawableName: String
    )
}
