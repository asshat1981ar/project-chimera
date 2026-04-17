package com.chimera.feature.map

import com.chimera.data.GameSessionManager
import com.chimera.data.MultiActMapNodeLoader
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.FactionStateDao
import com.chimera.database.dao.RumorPacketDao
import com.chimera.database.dao.SceneInstanceDao
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
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sceneInstanceDao: SceneInstanceDao = mock()
    private val rumorPacketDao: RumorPacketDao = mock()
    private val factionStateDao: FactionStateDao = mock()
    private val characterStateDao: CharacterStateDao = mock()
    private val mapNodeLoader: MultiActMapNodeLoader = mock()
    private val gameSessionManager: GameSessionManager = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = MapViewModel(
        sceneInstanceDao = sceneInstanceDao,
        rumorPacketDao = rumorPacketDao,
        factionStateDao = factionStateDao,
        characterStateDao = characterStateDao,
        mapNodeLoader = mapNodeLoader,
        gameSessionManager = gameSessionManager
    )

    @Test
    fun initialState_isNotNull() {
        val viewModel = buildViewModel()
        assertNotNull(viewModel.uiState)
    }
}
