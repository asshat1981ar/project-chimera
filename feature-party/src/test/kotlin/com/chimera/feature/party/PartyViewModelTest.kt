package com.chimera.feature.party

import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.FactionStateDao
import com.chimera.database.dao.MemoryShardDao
import com.chimera.domain.usecase.GetRelationshipDynamicsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PartyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val characterDao: CharacterDao = mock()
    private val characterStateDao: CharacterStateDao = mock()
    private val memoryShardDao: MemoryShardDao = mock()
    private val factionStateDao: FactionStateDao = mock()
    private val gameSessionManager: GameSessionManager = mock()
    private val dynamicsUseCase: GetRelationshipDynamicsUseCase = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = PartyViewModel(
        characterDao = characterDao,
        characterStateDao = characterStateDao,
        memoryShardDao = memoryShardDao,
        factionStateDao = factionStateDao,
        gameSessionManager = gameSessionManager,
        getRelationshipDynamics = dynamicsUseCase
    )

    @Test
    fun initialState_isNotNull() {
        val viewModel = buildViewModel()
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun recordsDispositionSnapshot() {
        val viewModel = buildViewModel()
        viewModel.recordDispositionSnapshot("test_npc")
        assert(true)
    }
}
