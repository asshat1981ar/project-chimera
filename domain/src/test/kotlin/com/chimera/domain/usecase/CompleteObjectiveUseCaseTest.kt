package com.chimera.domain.usecase

import com.chimera.data.repository.QuestRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class CompleteObjectiveUseCaseTest {

    private val questRepository: QuestRepository = mock()
    private fun useCase() = CompleteObjectiveUseCase(questRepository)

    @Test
    fun `byObjectiveId delegates to repository`() = runTest {
        useCase().byObjectiveId(1L, 2L)
        verify(questRepository).completeObjective(1L, 2L)
    }

    @Test
    fun `byScene delegates to repository`() = runTest {
        useCase().byScene(1L, "scene_1")
        verify(questRepository).completeObjectiveByScene(1L, "scene_1")
    }

    @Test
    fun `byNpc delegates to repository`() = runTest {
        useCase().byNpc(1L, "npc_1")
        verify(questRepository).completeObjectiveByNpc(1L, "npc_1")
    }

    @Test
    fun `byMapNode delegates to repository`() = runTest {
        useCase().byMapNode(1L, "node_1")
        verify(questRepository).completeObjectiveByMapNode(1L, "node_1")
    }
}
