package com.chimera.domain.usecase

import com.chimera.data.GameSessionManager
import com.chimera.data.NpcSeeder
import com.chimera.data.repository.SaveRepository
import javax.inject.Inject

class CreateSaveSlotUseCase @Inject constructor(
    private val saveRepository: SaveRepository,
    private val npcSeeder: NpcSeeder,
    private val gameSessionManager: GameSessionManager
) {
    suspend operator fun invoke(slotIndex: Int, playerName: String): Long {
        val slotId = saveRepository.createSlot(slotIndex, playerName)
        npcSeeder.seedNpcsForSlot(slotId)
        gameSessionManager.setActiveSlot(slotId)
        return slotId
    }
}
