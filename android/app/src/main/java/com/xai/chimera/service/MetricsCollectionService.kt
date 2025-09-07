package com.xai.chimera.service

import com.xai.chimera.dao.SelfOptMetricsDao
import com.xai.chimera.domain.SelfOptMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Service for collecting and storing self-optimization metrics during system operations
 */
class MetricsCollectionService(
    private val selfOptMetricsDao: SelfOptMetricsDao
) {
    
    companion object {
        private const val DEFAULT_CHUNK_SIZE = 1024
    }
    
    private var currentRunId: String? = null
    private var runStartTime: Long = 0
    private var fetchErrors: Int = 0
    private var memoryTracker = MemoryTracker()
    
    /**
     * Start a new metrics collection run
     */
    fun startRun(): String {
        currentRunId = UUID.randomUUID().toString()
        runStartTime = System.currentTimeMillis()
        fetchErrors = 0
        memoryTracker.reset()
        return currentRunId!!
    }
    
    /**
     * Record a fetch error during the run
     */
    fun recordFetchError() {
        fetchErrors++
    }
    
    /**
     * Complete the current run and store metrics
     */
    suspend fun completeRun(
        chunkSize: Int = DEFAULT_CHUNK_SIZE,
        successRate: Float = 1.0f,
        emotionProcessingTimeMs: Long = 0,
        consciousnessUpdateTimeMs: Long = 0,
        apiResponseTimeMs: Long = 0,
        playerEmotionVariance: Float = 0.0f,
        conversationComplexity: Float = 0.5f,
        additionalMetrics: Map<String, Float> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        
        val runId = currentRunId ?: return@withContext
        val processingTime = System.currentTimeMillis() - runStartTime
        val memoryUsage = memoryTracker.getCurrentMemoryUsageMb()
        val systemLoad = getSystemLoad()
        
        val metrics = SelfOptMetrics(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            runId = runId,
            processingTimeMs = processingTime,
            fetchErrors = fetchErrors,
            chunkSize = chunkSize,
            memoryUsageMb = memoryUsage,
            successRate = successRate,
            emotionProcessingTimeMs = emotionProcessingTimeMs,
            consciousnessUpdateTimeMs = consciousnessUpdateTimeMs,
            apiResponseTimeMs = apiResponseTimeMs,
            playerEmotionVariance = playerEmotionVariance,
            conversationComplexity = conversationComplexity,
            systemLoad = systemLoad,
            additionalMetrics = additionalMetrics
        )
        
        selfOptMetricsDao.insertMetrics(metrics)
        
        // Reset for next run
        currentRunId = null
        runStartTime = 0
        fetchErrors = 0
    }
    
    /**
     * Measure the execution time of a block and record it
     */
    suspend fun <T> measureAndRecord(
        operation: String,
        block: suspend () -> T
    ): T {
        var result: T
        val executionTime = measureTimeMillis {
            result = block()
        }
        
        // Store the measurement in additional metrics
        recordAdditionalMetric("${operation}_time_ms", executionTime.toFloat())
        
        return result
    }
    
    /**
     * Record an additional metric for the current run
     */
    fun recordAdditionalMetric(key: String, value: Float) {
        // For now, we'll just log it. In a full implementation,
        // you might want to accumulate these and include them in completeRun
        android.util.Log.d("MetricsCollection", "Additional metric: $key = $value")
    }
    
    /**
     * Get current system load estimate
     */
    private fun getSystemLoad(): Float {
        return try {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val totalMemory = runtime.totalMemory()
            (usedMemory.toFloat() / totalMemory.toFloat()).coerceIn(0f, 1f)
        } catch (e: Exception) {
            0.5f // Default moderate load
        }
    }
    
    /**
     * Helper class for tracking memory usage
     */
    private class MemoryTracker {
        private var baselineMemory: Long = 0
        
        fun reset() {
            baselineMemory = getCurrentMemoryBytes()
        }
        
        fun getCurrentMemoryUsageMb(): Float {
            val currentMemory = getCurrentMemoryBytes()
            return ((currentMemory - baselineMemory) / (1024 * 1024)).toFloat().coerceAtLeast(0f)
        }
        
        private fun getCurrentMemoryBytes(): Long {
            val runtime = Runtime.getRuntime()
            return runtime.totalMemory() - runtime.freeMemory()
        }
    }
}

/**
 * Extension functions to make metrics collection easier to use
 */
suspend fun <T> MetricsCollectionService.withMetrics(
    operation: String,
    chunkSize: Int = 1024,
    block: suspend () -> T
): T {
    val runId = startRun()
    return try {
        val result = measureAndRecord(operation, block)
        completeRun(chunkSize = chunkSize, successRate = 1.0f)
        result
    } catch (e: Exception) {
        recordFetchError()
        completeRun(chunkSize = chunkSize, successRate = 0.0f)
        throw e
    }
}