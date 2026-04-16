package com.chimera.data

import com.chimera.database.dao.FactionStateDao
import com.chimera.database.entity.FactionStateEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds the three canonical factions into [FactionStateEntity] rows for a
 * new save slot. Idempotent — uses upsert.
 */
@Singleton
class FactionSeeder @Inject constructor(
    private val factionStateDao: FactionStateDao
) {
    private val json = Json { ignoreUnknownKeys = true }

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

    suspend fun seedFactionsForSlot(slotId: Long) {
        factions.forEach { def ->
            factionStateDao.upsert(
                FactionStateEntity(
                    saveSlotId = slotId,
                    factionId = def.id,
                    factionName = def.name,
                    influence = def.initialInfluence,
                    playerStanding = 0f,
                    controlledLocationsJson = json.encodeToString(
                        ListSerializer(String.serializer()),
                        def.controlledLocations
                    )
                )
            )
        }
    }
}
