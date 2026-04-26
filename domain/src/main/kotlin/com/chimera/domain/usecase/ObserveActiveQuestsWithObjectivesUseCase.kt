package com.chimera.domain.usecase

import com.chimera.data.repository.QuestRepository
import com.chimera.model.QuestWithObjectives
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveQuestsWithObjectivesUseCase @Inject constructor(
    private val questRepository: QuestRepository
) {
    operator fun invoke(slotId: Long): Flow<List<QuestWithObjectives>> =
        questRepository.observeActiveQuestsWithObjectives(slotId)
}
