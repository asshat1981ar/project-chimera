package com.chimera.feature.settings

import com.chimera.data.AiMode
import com.chimera.data.AppSettings
import com.chimera.data.ChimeraPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryContractTest {

    private val testDispatcher = StandardTestDispatcher()
    private val preferences: ChimeraPreferences = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun settings_defaultTextScale_isOne() = runTest(testDispatcher) {
        whenever(preferences.settings).thenReturn(flowOf(AppSettings(textScale = 1.0f)))
        val viewModel = SettingsViewModel(preferences)
        advanceUntilIdle()
        assertEquals(1.0f, viewModel.settings.value.textScale)
    }

    @Test
    fun settings_defaultAiMode_isAuto() = runTest(testDispatcher) {
        whenever(preferences.settings).thenReturn(flowOf(AppSettings(aiMode = AiMode.AUTO)))
        val viewModel = SettingsViewModel(preferences)
        advanceUntilIdle()
        assertEquals(AiMode.AUTO, viewModel.settings.value.aiMode)
    }

    @Test
    fun settings_reducedMotion_reflectedInState() = runTest(testDispatcher) {
        whenever(preferences.settings).thenReturn(flowOf(AppSettings(reduceMotion = true)))
        val viewModel = SettingsViewModel(preferences)
        // Subscribe to trigger WhileSubscribed collection, then advance to drain the upstream
        val job = launch { viewModel.settings.collect {} }
        advanceUntilIdle()
        job.cancel()
        assertEquals(true, viewModel.settings.value.reduceMotion)
    }

    @Test
    fun setVoiceEnabled_callsPreferences() = runTest(testDispatcher) {
        whenever(preferences.settings).thenReturn(flowOf(AppSettings()))
        val viewModel = SettingsViewModel(preferences)
        viewModel.setVoiceEnabled(true)
        advanceUntilIdle()
        verify(preferences).setVoiceEnabled(true)
    }

    @Test
    fun setCloudSyncEnabled_callsPreferences() = runTest(testDispatcher) {
        whenever(preferences.settings).thenReturn(flowOf(AppSettings()))
        val viewModel = SettingsViewModel(preferences)
        viewModel.setCloudSyncEnabled(false)
        advanceUntilIdle()
        verify(preferences).setCloudSyncEnabled(false)
    }
}
