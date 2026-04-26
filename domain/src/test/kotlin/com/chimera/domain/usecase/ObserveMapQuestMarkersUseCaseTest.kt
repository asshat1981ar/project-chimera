package com.chimera.domain.usecase

import com.chimera.data.repository.QuestRepository
import com.chimera.model.MapQuestMarker
import com.chimera.model.QuestObjectiveStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ObserveMapQuestMarkersUseCaseTest {

    private val questRepository: QuestRepository = mock()
    private fun useCase() = ObserveMapQuestMarkersUseCase(questRepository)

    @Test
    fun `invoke emits markers for active node objectives`() = runTest {
        val markers = listOf(
            MapQuestMarker(
                mapNodeId = "forest_1",
                questId = 1L,
                objectiveId = 2L,
                title = "Find the hidden grove",
                isActiveTarget = true,
                isLockedTarget = false,
                status = QuestObjectiveStatus.ACTIVE
            )
        )
        whenever(questRepository.observeMapQuestMarkers("forest_1")).thenReturn(flowOf(markers))

        val result = useCase()("forest_1").first()

        assertEquals(1, result.size)
        assertEquals("Find the hidden grove", result[0].title)
        assertTrue(result[0].isActiveTarget)
    }

    @Test
    fun `invoke emits empty list when no markers`() = runTest {
        whenever(questRepository.observeMapQuestMarkers("empty_node")).thenReturn(flowOf(emptyList()))

        val result = useCase()("empty_node").first()

        assertTrue(result.isEmpty())
    }
}
