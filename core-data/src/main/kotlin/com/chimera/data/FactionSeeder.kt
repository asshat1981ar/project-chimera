package com.chimera.data

import com.chimera.database.dao.FactionStateDao
import com.chimera.database.entity.FactionStateEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FactionSeeder @Inject constructor(
    private val factionStateDao: FactionStateDao
) {
    data class FactionSeed(
        val id: String,
        val name: String,
        val influence: Float,
        val controlledLocationsJson: String
    )

    private val factions = listOf(
        FactionSeed(
            id = "hollow_remnant",
            name = "Hollow Remnant",
            influence = 0.6f,
            controlledLocationsJson = """["hollow_approach","old_fort","sunken_shrine"]"""
        ),
        FactionSeed(
            id = "reforged",
            name = "Reforged",
            influence = 0.5f,
            controlledLocationsJson = """["reforged_camp","ashen_gate","ember_sanctum"]"""
        ),
        FactionSeed(
            id = "unaffiliated",
            name = "Unaffiliated",
            influence = 0.3f,
            controlledLocationsJson = """["ash_market","memorial_field"]"""
        )
    )

    suspend fun seedFactionsForSlot(slotId: Long) {
        factions.forEach { seed ->
            val existing = factionStateDao.getByFaction(slotId, seed.id)
            if (existing == null) {
                factionStateDao.upsert(
                    FactionStateEntity(
                        saveSlotId = slotId,
                        factionId = seed.id,
                        factionName = seed.name,
                        influence = seed.influence,
                        playerStanding = 0f,
                        controlledLocationsJson = seed.controlledLocationsJson
                    )
                )
            }
        }
    }
}
