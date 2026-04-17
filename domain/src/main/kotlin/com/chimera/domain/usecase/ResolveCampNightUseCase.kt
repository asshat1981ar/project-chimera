package com.chimera.domain.usecase

import com.chimera.data.NightEvent
import com.chimera.data.NightEventChoice
import com.chimera.data.NightEventProvider
import com.chimera.data.RumorService
import javax.inject.Inject

data class CampNightOutcome(
    val event: NightEvent,
    val chosenOption: NightEventChoice,
    val outcome: String,
    val moraleChange: Float
)

class ResolveCampNightUseCase @Inject constructor(
    private val nightEventProvider: NightEventProvider,
    private val rumorService: RumorService
) {
    fun selectEvent(morale: Float): NightEvent =
        nightEventProvider.getRandomEvent(morale)

    suspend fun resolve(slotId: Long, event: NightEvent, choice: NightEventChoice): CampNightOutcome {
        // Advance day: decay rumors
        rumorService.advanceDay(slotId)

        return CampNightOutcome(
            event = event,
            chosenOption = choice,
            outcome = choice.outcome,
            moraleChange = choice.moraleDelta
        )
    }
}
