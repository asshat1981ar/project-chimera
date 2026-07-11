package com.chimera.data

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics event logging, backed by Firebase Analytics when a real Firebase project is
 * configured and the player has opted in.
 *
 * Like [CrashReporter], this is inert scaffolding until `google-services.json` is present in
 * `app/` -- without it, [FirebaseApp.initializeApp] returns null and every call here becomes a
 * no-op rather than a crash. Independently of that, every call also respects
 * [AppSettings.analyticsOptIn] (read fresh each time, the same way [ChimeraPreferences] is
 * already consulted for `voiceEnabled` elsewhere) so the existing Settings toggle -- previously
 * read but never acted on -- actually gates something now.
 */
@Singleton
class AnalyticsTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: ChimeraPreferences
) {
    private val analytics: FirebaseAnalytics? by lazy {
        try {
            FirebaseApp.initializeApp(context)?.let { FirebaseAnalytics.getInstance(context) }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not configured, analytics events stay local-only: ${e.message}")
            null
        }
    }

    suspend fun logEvent(name: String, params: Map<String, String> = emptyMap()) {
        val instance = analytics ?: return
        if (!isOptedIn()) return
        instance.logEvent(name, Bundle().apply { params.forEach { (key, value) -> putString(key, value) } })
    }

    suspend fun setUserProperty(name: String, value: String) {
        val instance = analytics ?: return
        if (!isOptedIn()) return
        instance.setUserProperty(name, value)
    }

    private suspend fun isOptedIn(): Boolean =
        preferences.settings.map { it.analyticsOptIn }.first()

    companion object {
        private const val TAG = "AnalyticsTracker"
    }
}
