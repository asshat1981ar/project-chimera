package com.chimera.feature.settings

import com.chimera.data.AppSettings
import com.chimera.data.ChimeraPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val preferences: ChimeraPreferences = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(preferences.settings).thenReturn(flowOf(AppSettings()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = SettingsViewModel(
        preferences = preferences
    )

    @Test
    fun initialState_isNotNull() {
        val viewModel = buildViewModel()
        assertNotNull(viewModel.settings)
    }

    @Test
    fun setTextScale_callsPreferencesSetTextScale() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        viewModel.setTextScale(1.2f)
        advanceUntilIdle()
        verify(preferences).setTextScale(1.2f)
    }

    @Test
    fun toggleAiMode_callsPreferencesSetAiMode() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        viewModel.toggleAiMode()
        advanceUntilIdle()
        verify(preferences).setAiMode(any())
    }

    @Test
    fun setAnalyticsOptIn_callsPreferencesWithTrue() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        viewModel.setAnalyticsOptIn(true)
        advanceUntilIdle()
        verify(preferences).setAnalyticsOptIn(true)
    }
}
