package com.chimera.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crash monitoring, backed by Firebase Crashlytics when a real Firebase project is configured.
 *
 * Firebase is only actually active once `google-services.json` has been dropped into `app/`
 * (see README's "Monetization & Analytics" section) -- the Gradle plugin that generates the
 * resources Firebase needs is applied conditionally on that file existing, so without it
 * [FirebaseApp.initializeApp] returns null here and this class quietly falls back to its
 * previous Log-only behavior instead of crashing.
 */
@Singleton
class CrashReporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val crashlytics: FirebaseCrashlytics? by lazy {
        try {
            FirebaseApp.initializeApp(context)?.let { FirebaseCrashlytics.getInstance() }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not configured, crash reporting stays local-only: ${e.message}")
            null
        }
    }

    fun initialize() {
        if (crashlytics != null) {
            Log.d(TAG, "CrashReporter initialized (Firebase Crashlytics active)")
        } else {
            Log.d(TAG, "CrashReporter initialized (no google-services.json -- local logging only)")
        }
    }

    fun log(throwable: Throwable, context: String = "") {
        Log.e(TAG, "Crash: $context", throwable)
        crashlytics?.recordException(throwable)
    }

    fun log(message: String) {
        Log.w(TAG, "Event: $message")
        crashlytics?.log(message)
    }

    fun setUser(slotId: Long) {
        Log.d(TAG, "Active user: slot $slotId")
        crashlytics?.setUserId(slotId.toString())
    }

    companion object {
        private const val TAG = "CrashReporter"
    }
}
