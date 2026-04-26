package com.chimera.domain.usecase

import com.chimera.data.repository.QuestRepository
import com.chimera.model.QuestObjective
import com.chimera.model.QuestObjectiveStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Observe active quests for the current save slot as a flat list of objective summaries.
 * Used to render the HUD objective panel on Home and other screens.
 */
class ObserveActiveObjectiveSummariesUseCase @Inject constructor(
    private val questRepository: QuestRepository
) {
    operator fun invoke(slotId: Long): Flow<List<com.chimera.model.ActiveObjectiveSummary>> =
        questRepository.observeActiveObjectiveSummaries(slotId)
}
