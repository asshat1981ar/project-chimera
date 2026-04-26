package com.chimera

import android.app.Application
import android.os.Build
import android.os.Trace
import android.view.Window
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
 *
 * ## Performance Tracking
 * - Startup tracing: Application.onCreate to first frame
 * - FrameMetricsAggregator: Jank measurement (target <5%)
 * - Targets: Startup <500ms, Memory <100MB, Jank <5%
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

    /** FrameMetricsAggregator for jank measurement (API 24+) */
    private var frameMetricsAggregator: FrameMetricsAggregatorWrapper? = null

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

    /**
     * Start tracking frames for a window (call from Activity/Compose)
     */
    fun startFrameTracking(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            frameMetricsAggregator = FrameMetricsAggregatorWrapper()
            frameMetricsAggregator?.addWindow(window)
        }
    }

    /**
     * Stop tracking and get jank metrics
     */
    fun stopFrameTracking(): FrameMetricsResult? {
        return frameMetricsAggregator?.removeAndClear()
    }

    /**
     * Get current jank percentage (frames >16ms or >700ms)
     */
    fun getJankPercentage(): Float {
        return frameMetricsAggregator?.getJankPercentage() ?: 0f
    }

    private fun beginStartupTrace(section: String) {
        Trace.beginSection(section)
    }

    private fun endStartupTrace() {
        Trace.endSection()
    }
}

/**
 * Wrapper for FrameMetricsAggregator to handle API differences
 */
class FrameMetricsAggregatorWrapper {
    fun addWindow(window: Window) {
        // No-op until frame metrics are wired through a supported AndroidX aggregator.
    }

    fun removeAndClear(): FrameMetricsResult {
        return FrameMetricsResult()
    }

    fun getJankPercentage(): Float {
        return 0f
    }
}

/**
 * Frame metrics result data class
 */
data class FrameMetricsResult(
    val totalFrames: Int = 0,
    val slowFrames: Int = 0,      // Frames >16ms (missed 60fps)
    val frozenFrames: Int = 0,    // Frames >700ms (ANR threshold)
    val jankPercentage: Float = 0f,
    val averageFrameTimeMs: Float = 0f,
    val p90FrameTimeMs: Float = 0f,
    val p99FrameTimeMs: Float = 0f
) {
    override fun toString(): String {
        return "Frames: $totalFrames | Slow: $slowFrames | Frozen: $frozenFrames | " +
                "Jank: ${"%.2f".format(jankPercentage)}% | " +
                "Avg: ${"%.1f".format(averageFrameTimeMs)}ms | " +
                "P90: ${"%.1f".format(p90FrameTimeMs)}ms | P99: ${"%.1f".format(p99FrameTimeMs)}ms"
    }
}
