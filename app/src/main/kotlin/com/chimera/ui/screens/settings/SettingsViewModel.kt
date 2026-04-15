package com.chimera.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.AiMode
import com.chimera.data.AppSettings
import com.chimera.data.ChimeraPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: ChimeraPreferences
) : ViewModel() {

    val settings: StateFlow<AppSettings> = preferences.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setTextScale(scale: Float) {
        viewModelScope.launch { preferences.setTextScale(scale) }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch { preferences.setReduceMotion(enabled) }
    }

    fun toggleAiMode() {
        viewModelScope.launch {
            val current = settings.value.aiMode
            val next = if (current == AiMode.AUTO) AiMode.OFFLINE_ONLY else AiMode.AUTO
            preferences.setAiMode(next)
        }
    }

    fun setAnalyticsOptIn(optIn: Boolean) {
        viewModelScope.launch { preferences.setAnalyticsOptIn(optIn) }
    }
}
