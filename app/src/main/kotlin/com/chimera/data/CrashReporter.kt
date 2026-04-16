package com.chimera.data

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crash monitoring placeholder. Replace with Firebase Crashlytics or Sentry
 * when ready for production.
 *
 * To integrate Firebase Crashlytics:
 * 1. Add google-services.json to app/
 * 2. Add Firebase BOM + Crashlytics dependencies
 * 3. Replace log() with FirebaseCrashlytics.getInstance().recordException()
 * 4. Replace setUser() with FirebaseCrashlytics.getInstance().setUserId()
 */
@Singleton
class CrashReporter @Inject constructor() {

    fun initialize() {
        Log.d(TAG, "CrashReporter initialized (placeholder -- no remote reporting)")
    }

    fun log(throwable: Throwable, context: String = "") {
        Log.e(TAG, "Crash: $context", throwable)
    }

    fun log(message: String) {
        Log.w(TAG, "Event: $message")
    }

    fun setUser(slotId: Long) {
        Log.d(TAG, "Active user: slot $slotId")
    }

    companion object {
        private const val TAG = "CrashReporter"
    }
}
