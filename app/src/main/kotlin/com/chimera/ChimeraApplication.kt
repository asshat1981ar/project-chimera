package com.chimera

import android.app.Application
import android.os.Trace
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point.
 * Implements [Configuration.Provider] so WorkManager uses [HiltWorkerFactory],
 * enabling constructor injection in [com.chimera.workers.NpcPortraitSyncWorker]
 * and any future workers.
 *
 * IMPORTANT: Do NOT call WorkManager.initialize() anywhere else — the on-demand
 * initializer picks up Configuration.Provider automatically.
 */
@HiltAndroidApp
class ChimeraApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /** Timestamp when application started loading */
    val startupTimestamp: Long = System.currentTimeMillis()

    /** Track if we're in the startup phase */
    private var startupTraceActive = false

    override fun onCreate() {
        super.onCreate()
        beginStartupTrace("ChimeraApplication.onCreate")
        startupTraceActive = true
    }

    /**
     * Call this from MainActivity when the first frame is drawn
     * to complete the startup trace.
     */
    fun onFirstFrameDrawn() {
        if (startupTraceActive) {
            endStartupTrace()
            startupTraceActive = false
            val startupTimeMs = System.currentTimeMillis() - startupTimestamp
            android.util.Log.d("ChimeraPerformance", "Startup time: ${startupTimeMs}ms (target: <500ms)")
        }
    }

    private fun beginStartupTrace(section: String) {
        Trace.beginSection(section)
    }

    private fun endStartupTrace() {
        Trace.endSection()
    }
}
