package com.chimera

import android.app.Application
import android.os.Build
import android.os.Trace
import android.view.FrameMetrics
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
    private val aggregator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        android.view.FrameMetricsAggregator()
    } else {
        null
    }

    private var trackingStarted = false

    fun addWindow(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            aggregator?.addWindow(window)
            trackingStarted = true
        }
    }

    fun removeAndClear(): FrameMetricsResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || aggregator == null) {
            return FrameMetricsResult()
        }

        val metrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            aggregator?.metrics
        } else {
            @Suppress("DEPRECATION")
            aggregator?.getMetrics()
        }

        val result = metrics?.let {
            val totalFrames = it.totalFrames
            val slowFrames = it.slowFrames  // >16ms
            val frozenFrames = it.frozenFrames  // >700ms
            val jankPercentage = if (totalFrames > 0) {
                ((slowFrames + frozenFrames) / totalFrames.toFloat()) * 100
            } else 0f

            FrameMetricsResult(
                totalFrames = totalFrames,
                slowFrames = slowFrames,
                frozenFrames = frozenFrames,
                jankPercentage = jankPercentage,
                averageFrameTimeMs = it.averageFrameTimeMs,
                p90FrameTimeMs = it.percentile90FrameTimeMs,
                p99FrameTimeMs = it.percentile99FrameTimeMs
            )
        } ?: FrameMetricsResult()

        aggregator?.clear()
        trackingStarted = false
        return result
    }

    fun getJankPercentage(): Float {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !trackingStarted) {
            return 0f
        }

        val metrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            aggregator?.metrics
        } else {
            @Suppress("DEPRECATION")
            aggregator?.getMetrics()
        }

        return metrics?.let {
            if (it.totalFrames > 0) {
                ((it.slowFrames + it.frozenFrames) / it.totalFrames.toFloat()) * 100
            } else 0f
        } ?: 0f
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
