package com.xai.chimera.service

import android.content.Context
import com.xai.chimera.database.ChimeraDatabase
import com.xai.chimera.domain.AnalysisInsights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Integration service that ties together metrics collection, analysis, and insights
 * This demonstrates how to use the automated analysis system
 */
class IntegratedAnalysisService(
    private val context: Context
) {
    
    private val database by lazy { ChimeraDatabase.getDatabase(context) }
    private val metricsCollectionService by lazy { 
        MetricsCollectionService(database.selfOptMetricsDao()) 
    }
    private val analysisService by lazy { 
        AnalysisService(database.selfOptMetricsDao()) 
    }
    private val analysisManager by lazy { 
        AnalysisManager(context) 
    }
    
    /**
     * Initialize the automated analysis system
     */
    suspend fun initializeAnalysisSystem(
        enablePeriodicAnalysis: Boolean = true,
        periodicAnalysisHours: Long = 6L
    ) = withContext(Dispatchers.IO) {
        
        if (enablePeriodicAnalysis) {
            analysisManager.startPeriodicAnalysis(
                periodHours = periodicAnalysisHours,
                analysisConfig = AnalysisConfig(
                    periodDays = 7,
                    limitRuns = 100,
                    enableTrendAnalysis = true,
                    enableAnomalyDetection = true,
                    enableBottleneckIdentification = true
                )
            )
            android.util.Log.i("IntegratedAnalysis", 
                             "Periodic analysis scheduled every $periodicAnalysisHours hours")
        }
    }
    
    /**
     * Enhanced dialogue generation with integrated metrics collection
     */
    suspend fun <T> executeWithMetrics(
        operation: String,
        chunkSize: Int = 1024,
        block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        
        return@withContext metricsCollectionService.withMetrics(operation, chunkSize) {
            block()
        }
    }
    
    /**
     * Run immediate analysis and get insights
     */
    suspend fun getImmediateInsights(
        periodDays: Int = 7,
        limitRuns: Int = 50
    ): AnalysisInsights = withContext(Dispatchers.IO) {
        
        return@withContext analysisService.performAnalysis(periodDays, limitRuns)
    }
    
    /**
     * Apply dynamic adjustments based on recent analysis
     */
    suspend fun applyDynamicAdjustments(): Map<String, Float> = withContext(Dispatchers.IO) {
        
        val insights = analysisService.performAnalysis(periodDays = 3, limitRuns = 30)
        val adjustments = insights.dynamicAdjustments
        
        if (adjustments.isNotEmpty()) {
            android.util.Log.i("IntegratedAnalysis", 
                             "Applying dynamic adjustments: $adjustments")
            
            // Store adjustments in shared preferences or apply to system
            storeAdjustments(adjustments)
        }
        
        return@withContext adjustments
    }
    
    /**
     * Get health report of the system
     */
    suspend fun getSystemHealthReport(): SystemHealthReport = withContext(Dispatchers.IO) {
        
        val insights = analysisService.performAnalysis(periodDays = 1, limitRuns = 20)
        
        return@withContext SystemHealthReport(
            overallScore = insights.overallPerformanceScore,
            criticalIssues = insights.anomalies.filter { 
                it.severity.ordinal >= AnomalySeverity.HIGH.ordinal 
            }.size,
            activeBottlenecks = insights.bottlenecks.size,
            trendSummary = insights.trends.groupBy { it.trend }.mapValues { it.value.size },
            recommendations = insights.recommendations.take(5),
            lastAnalysisTime = insights.analysisTimestamp
        )
    }
    
    /**
     * Trigger manual analysis (useful for debugging or immediate insights)
     */
    suspend fun triggerManualAnalysis(): AnalysisInsights = withContext(Dispatchers.IO) {
        
        analysisManager.triggerImmediateAnalysis()
        
        // Return the analysis results
        return@withContext analysisService.performAnalysis()
    }
    
    /**
     * Clean up old metrics to maintain database performance
     */
    suspend fun performMaintenanceCleanup(
        retentionDays: Int = 30
    ): Int = withContext(Dispatchers.IO) {
        
        val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        val deletedCount = database.selfOptMetricsDao().deleteOldMetrics(cutoffTime)
        
        android.util.Log.i("IntegratedAnalysis", 
                         "Maintenance cleanup: deleted $deletedCount old metrics")
        
        return@withContext deletedCount
    }
    
    /**
     * Stop the automated analysis system
     */
    fun stopAnalysisSystem() {
        analysisManager.stopPeriodicAnalysis()
        android.util.Log.i("IntegratedAnalysis", "Automated analysis system stopped")
    }
    
    /**
     * Store dynamic adjustments for application
     */
    private fun storeAdjustments(adjustments: Map<String, Float>) {
        // Store in SharedPreferences for later retrieval by other services
        val prefs = context.getSharedPreferences("chimera_adjustments", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        adjustments.forEach { (key, value) ->
            editor.putFloat(key, value)
        }
        
        editor.putLong("adjustments_timestamp", System.currentTimeMillis())
        editor.apply()
    }
    
    /**
     * Get stored dynamic adjustments
     */
    fun getStoredAdjustments(): Map<String, Float> {
        val prefs = context.getSharedPreferences("chimera_adjustments", Context.MODE_PRIVATE)
        val timestamp = prefs.getLong("adjustments_timestamp", 0)
        
        // Only return adjustments if they're less than 24 hours old
        return if (System.currentTimeMillis() - timestamp < 24 * 60 * 60 * 1000L) {
            prefs.all.filterKeys { it != "adjustments_timestamp" }
                .mapValues { (it.value as? Float) ?: 0f }
        } else {
            emptyMap()
        }
    }
}

/**
 * Data class for system health reporting
 */
data class SystemHealthReport(
    val overallScore: Float,
    val criticalIssues: Int,
    val activeBottlenecks: Int,
    val trendSummary: Map<TrendDirection, Int>,
    val recommendations: List<String>,
    val lastAnalysisTime: Long
) {
    fun getHealthStatus(): HealthStatus {
        return when {
            overallScore >= 0.8f && criticalIssues == 0 -> HealthStatus.EXCELLENT
            overallScore >= 0.6f && criticalIssues <= 1 -> HealthStatus.GOOD
            overallScore >= 0.4f && criticalIssues <= 3 -> HealthStatus.FAIR
            overallScore >= 0.2f -> HealthStatus.POOR
            else -> HealthStatus.CRITICAL
        }
    }
}

enum class HealthStatus {
    EXCELLENT, GOOD, FAIR, POOR, CRITICAL
}