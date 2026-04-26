package com.chimera.domain.usecase

import com.chimera.data.repository.QuestRepository
import com.chimera.model.ActiveObjectiveSummary
import com.chimera.model.ObjectivePrimaryAction
import com.chimera.model.Quest
import com.chimera.model.QuestStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ObserveActiveObjectiveSummariesUseCaseTest {

    private val questRepository: QuestRepository = mock()
    private fun useCase() = ObserveActiveObjectiveSummariesUseCase(questRepository)

    @Test
    fun `invoke emits active objective summaries`() = runTest {
        // Arrange
        val summaries = listOf(
            ActiveObjectiveSummary(
                questId = 1L,
                objectiveId = 2L,
                title = "Reach the watchtower",
                storyContext = "The watchtower looms ahead",
                primaryAction = ObjectivePrimaryAction.OPEN_MAP
            )
        )
        whenever(questRepository.observeActiveObjectiveSummaries(1L)).thenReturn(flowOf(summaries))

        // Act
        val result = useCase()(1L).first()

        // Assert
        assertEquals(1, result.size)
        assertEquals("Reach the watchtower", result[0].title)
        assertEquals(ObjectivePrimaryAction.OPEN_MAP, result[0].primaryAction)
    }

    @Test
    fun `invoke emits empty list when no active quests`() = runTest {
        // Arrange
        whenever(questRepository.observeActiveObjectiveSummaries(1L)).thenReturn(flowOf(emptyList()))

        // Act
        val result = useCase()(1L).first()

        // Assert
        assertTrue(result.isEmpty())
    }
}
