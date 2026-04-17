package com.chimera.domain.usecase

import com.chimera.data.NightEvent
import com.chimera.data.NightEventChoice
import com.chimera.data.NightEventProvider
import com.chimera.data.RumorService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ResolveCampNightUseCaseTest {

    private val nightEventProvider: NightEventProvider = mock()
    private val rumorService: RumorService = mock()

    private fun useCase() = ResolveCampNightUseCase(nightEventProvider, rumorService)

    private val testChoice = NightEventChoice(
        text = "Listen quietly",
        moraleDelta = 0.05f,
        outcome = "Morale rises slightly."
    )
    private val testEvent = NightEvent(
        id = "campfire_stories",
        title = "Campfire Stories",
        narrative = "The fire crackles.",
        choices = listOf(testChoice)
    )

    @Test
    fun `selectEvent delegates to nightEventProvider`() {
        whenever(nightEventProvider.getRandomEvent(0.6f)).thenReturn(testEvent)

        val result = useCase().selectEvent(0.6f)

        assertEquals(testEvent, result)
        verify(nightEventProvider).getRandomEvent(0.6f)
    }

    @Test
    fun `resolve advances rumors`() = runTest {
        useCase().resolve(slotId = 1L, event = testEvent, choice = testChoice)

        verify(rumorService).advanceDay(1L)
    }

    @Test
    fun `resolve returns outcome matching choice`() = runTest {
        val outcome = useCase().resolve(slotId = 1L, event = testEvent, choice = testChoice)

        assertEquals(testChoice.outcome, outcome.outcome)
        assertEquals(testChoice.moraleDelta, outcome.moraleChange)
        assertEquals(testEvent, outcome.event)
        assertEquals(testChoice, outcome.chosenOption)
    }

    @Test
    fun `resolve with negative morale delta preserves sign`() = runTest {
        val badChoice = NightEventChoice(
            text = "Keep watch instead",
            moraleDelta = -0.02f,
            outcome = "Night passes uneventfully."
        )

        val outcome = useCase().resolve(slotId = 2L, event = testEvent, choice = badChoice)

        assertEquals(-0.02f, outcome.moraleChange)
    }

    @Test
    fun `outcome is not null for any valid event and choice`() = runTest {
        val outcome = useCase().resolve(slotId = 1L, event = testEvent, choice = testChoice)

        assertNotNull(outcome)
        assertNotNull(outcome.event)
        assertNotNull(outcome.outcome)
    }
}
