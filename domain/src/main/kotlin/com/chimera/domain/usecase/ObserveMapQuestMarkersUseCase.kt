package com.chimera.domain.usecase

import com.chimera.data.repository.QuestRepository
import com.chimera.model.MapQuestMarker
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observe map quest markers for a specific map node.
 * Drives the quest indicator overlays on the map screen.
 */
class ObserveMapQuestMarkersUseCase @Inject constructor(
    private val questRepository: QuestRepository
) {
    operator fun invoke(nodeId: String): Flow<List<MapQuestMarker>> =
        questRepository.observeMapQuestMarkers(nodeId)
}
