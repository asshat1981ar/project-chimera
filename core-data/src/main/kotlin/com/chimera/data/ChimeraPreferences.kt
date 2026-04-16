package com.chimera.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chimera_settings")

data class AppSettings(
    val textScale: Float = 1.0f,
    val reduceMotion: Boolean = false,
    val aiMode: AiMode = AiMode.AUTO,
    val analyticsOptIn: Boolean = false,
    val tutorialComplete: Boolean = false,
    val voiceEnabled: Boolean = false,  // NPC TTS voice — off by default (battery consideration)
    val cloudSyncEnabled: Boolean = true  // Download newer cloud save on slot select
)

enum class AiMode(val label: String) {
    AUTO("Auto"),
    OFFLINE_ONLY("Offline Only")
}

@Singleton
class ChimeraPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val AI_MODE = stringPreferencesKey("ai_mode")
        val ANALYTICS_OPT_IN = booleanPreferencesKey("analytics_opt_in")
        val TUTORIAL_COMPLETE = booleanPreferencesKey("tutorial_complete")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            textScale = prefs[Keys.TEXT_SCALE] ?: 1.0f,
            reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
            aiMode = prefs[Keys.AI_MODE]?.let { AiMode.valueOf(it) } ?: AiMode.AUTO,
            analyticsOptIn = prefs[Keys.ANALYTICS_OPT_IN] ?: false,
            tutorialComplete = prefs[Keys.TUTORIAL_COMPLETE] ?: false,
            voiceEnabled = prefs[Keys.VOICE_ENABLED] ?: false,
            cloudSyncEnabled = prefs[Keys.CLOUD_SYNC_ENABLED] ?: true
        )
    }

    suspend fun setTextScale(scale: Float) {
        context.dataStore.edit { it[Keys.TEXT_SCALE] = scale.coerceIn(0.8f, 1.5f) }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REDUCE_MOTION] = enabled }
    }

    suspend fun setAiMode(mode: AiMode) {
        context.dataStore.edit { it[Keys.AI_MODE] = mode.name }
    }

    suspend fun setAnalyticsOptIn(optIn: Boolean) {
        context.dataStore.edit { it[Keys.ANALYTICS_OPT_IN] = optIn }
    }

    suspend fun setTutorialComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.TUTORIAL_COMPLETE] = complete }
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VOICE_ENABLED] = enabled }
    }

    suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CLOUD_SYNC_ENABLED] = enabled }
    }
}
