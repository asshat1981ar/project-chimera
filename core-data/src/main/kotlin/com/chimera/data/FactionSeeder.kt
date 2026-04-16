package com.chimera.data

import com.chimera.database.dao.FactionStateDao
import com.chimera.database.entity.FactionStateEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds the three canonical factions into [FactionStateEntity] rows for a
 * new save slot.
 *
 * Faction definitions are authored here rather than in a JSON file because
 * they are fixed game-design constants (names, initial influence, controlled
 * locations) that don't change between builds.
 *
 * Call this once during new-game creation alongside [MultiActNpcSeeder] and
 * [CraftingRecipeSeeder].
 *
 * Initial standing is 0.0 (neutral) for all factions. Player choices during
 * dialogue scenes adjust standing via [FactionStateDao.adjustStanding].
 */
@Singleton
class FactionSeeder @Inject constructor(
    private val factionStateDao: FactionStateDao
) {
    data class FactionDef(
        val id: String,
        val name: String,
        val initialInfluence: Float,
        val controlledLocations: List<String>
    )

    private val factions = listOf(
        FactionDef(
            id = "hollow_remnant",
            name = "The Hollow Remnant",
            initialInfluence = 0.6f,
            controlledLocations = listOf(
                "hollow_gate", "outer_ruins", "watchtower",
                "merchants_rest", "deep_hollow", "deserters_camp",
                "broken_shrine", "hollow_approach"
            )
        ),
        FactionDef(
            id = "reforged",
            name = "The Reforged",
            initialInfluence = 0.4f,
            controlledLocations = listOf(
                "reforged_camp", "ember_sanctum", "ash_market",
                "ashen_throne", "reforged_fleet", "tidewall"
            )
        ),
        FactionDef(
            id = "unaffiliated",
            name = "Unaffiliated",
            initialInfluence = 0.2f,
            controlledLocations = listOf(
                "memorial_field", "ruined_library", "smugglers_cove",
                "salvage_yard", "drowned_temple", "tidal_lab", "tide_amphitheater"
            )
        )
    )

    /**
     * Seeds all three factions for [slotId]. Idempotent — uses upsert.
     */
    suspend fun seedFactionsForSlot(slotId: Long) {
        factions.forEach { def ->
            factionStateDao.upsert(
                FactionStateEntity(
                    saveSlotId = slotId,
                    factionId = def.id,
                    factionName = def.name,
                    influence = def.initialInfluence,
                    playerStanding = 0f,
                    controlledLocationsJson = kotlinx.serialization.json.Json
                        .encodeToString(
                            kotlinx.serialization.builtins.ListSerializer(
                                kotlinx.serialization.builtins.serializer<String>()
                            ),
                            def.controlledLocations
                        )
                )
            )
        }
    }
}
