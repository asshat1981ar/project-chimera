package com.chimera

import android.app.Application
import android.os.Trace
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.chimera.core.data.sprites.SpriteManifest
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application entry point.
 * Implements [Configuration.Provider] so WorkManager uses [HiltWorkerFactory],
 * enabling constructor injection in [com.chimera.workers.NpcPortraitSyncWorker]
 * and any future workers.
 *
 * IMPORTANT: Do NOT call WorkManager.initialize() anywhere else — the on-demand
 * initializer picks up Configuration.Provider automatically.
 *
 * v2 (2026-07-14, WU-04): initializes the sprite manifest off the main thread
 * at startup. Non-blocking: screens resolving sprites before initialization
 * completes simply get null and render their legacy fallbacks.
 */
@HiltAndroidApp
class ChimeraApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var spriteManifest: SpriteManifest

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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

        // Sprite manifest: load off the main thread; idempotent, fail-soft.
        applicationScope.launch {
            spriteManifest.initialize()
        }
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
