package com.xai.chimera.service

import android.content.Context
import androidx.work.*
import com.xai.chimera.database.ChimeraDatabase
import com.xai.chimera.domain.AnalysisInsights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Periodic background worker for automated analysis and insight generation
 */
class PeriodicAnalysisWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "periodic_analysis_work"
        const val TAG = "PeriodicAnalysisWorker"
        private const val DEFAULT_PERIOD_HOURS = 6L
        
        /**
         * Schedule periodic analysis work
         */
        fun schedulePeriodicAnalysis(
            context: Context,
            periodHours: Long = DEFAULT_PERIOD_HOURS
        ) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicAnalysisWorker>(
                periodHours, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }
        
        /**
         * Cancel periodic analysis work
         */
        fun cancelPeriodicAnalysis(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
        
        /**
         * Run one-time analysis immediately
         */
        fun runOneTimeAnalysis(context: Context) {
            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<PeriodicAnalysisWorker>()
                .addTag(TAG)
                .build()
            
            WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
        }
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = ChimeraDatabase.getDatabase(applicationContext)
            val analysisService = AnalysisService(database.selfOptMetricsDao())
            
            // Get analysis parameters from input data
            val periodDays = inputData.getInt("period_days", 7)
            val limitRuns = inputData.getInt("limit_runs", 100)
            
            // Perform the analysis
            val insights = analysisService.performAnalysis(periodDays, limitRuns)
            
            // Store insights for later retrieval
            storeInsights(insights)
            
            // Log summary of analysis
            logAnalysisSummary(insights)
            
            Result.success(createOutputData(insights))
            
        } catch (exception: Exception) {
            // Log error for debugging
            android.util.Log.e(TAG, "Analysis failed: ${exception.message}", exception)
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure(
                    workDataOf("error" to (exception.message ?: "Unknown error"))
                )
            }
        }
    }
    
    /**
     * Store analysis insights (could be in database, shared preferences, or sent to analytics)
     */
    private suspend fun storeInsights(insights: AnalysisInsights) {
        // For now, we'll use Android's built-in logging
        // In a production app, you might want to store this in the database
        // or send to an analytics service
        
        android.util.Log.i(TAG, "Analysis completed: ${insights.totalRuns} runs analyzed")
        android.util.Log.i(TAG, "Performance score: ${insights.overallPerformanceScore}")
        android.util.Log.i(TAG, "Trends found: ${insights.trends.size}")
        android.util.Log.i(TAG, "Anomalies detected: ${insights.anomalies.size}")
        android.util.Log.i(TAG, "Bottlenecks identified: ${insights.bottlenecks.size}")
        
        // Store recommendations and adjustments for later use
        if (insights.recommendations.isNotEmpty()) {
            android.util.Log.i(TAG, "Recommendations: ${insights.recommendations.joinToString("; ")}")
        }
        
        if (insights.dynamicAdjustments.isNotEmpty()) {
            android.util.Log.i(TAG, "Dynamic adjustments: ${insights.dynamicAdjustments}")
        }
    }
    
    /**
     * Log a summary of the analysis results
     */
    private fun logAnalysisSummary(insights: AnalysisInsights) {
        val summary = buildString {
            appendLine("=== Automated Analysis Summary ===")
            appendLine("Analysis timestamp: ${java.util.Date(insights.analysisTimestamp)}")
            appendLine("Period analyzed: ${insights.periodAnalyzed.first} to ${insights.periodAnalyzed.second}")
            appendLine("Total runs: ${insights.totalRuns}")
            appendLine("Performance score: ${"%.2f".format(insights.overallPerformanceScore * 100)}%")
            appendLine()
            
            if (insights.trends.isNotEmpty()) {
                appendLine("Trends:")
                insights.trends.forEach { trend ->
                    appendLine("  - ${trend.metricName}: ${trend.trend} (confidence: ${"%.1f".format(trend.confidence * 100)}%)")
                }
                appendLine()
            }
            
            if (insights.anomalies.isNotEmpty()) {
                appendLine("Anomalies:")
                insights.anomalies.groupBy { it.severity }.forEach { (severity, anomalies) ->
                    appendLine("  $severity: ${anomalies.size}")
                }
                appendLine()
            }
            
            if (insights.bottlenecks.isNotEmpty()) {
                appendLine("Bottlenecks:")
                insights.bottlenecks.forEach { bottleneck ->
                    appendLine("  - ${bottleneck.componentName}: ${bottleneck.bottleneckType} (impact: ${"%.1f".format(bottleneck.impact * 100)}%)")
                }
                appendLine()
            }
            
            if (insights.recommendations.isNotEmpty()) {
                appendLine("Recommendations:")
                insights.recommendations.forEach { rec ->
                    appendLine("  - $rec")
                }
            }
        }
        
        android.util.Log.i(TAG, summary)
    }
    
    /**
     * Create output data for the work result
     */
    private fun createOutputData(insights: AnalysisInsights): Data {
        return workDataOf(
            "analysis_timestamp" to insights.analysisTimestamp,
            "total_runs" to insights.totalRuns,
            "performance_score" to insights.overallPerformanceScore,
            "trends_count" to insights.trends.size,
            "anomalies_count" to insights.anomalies.size,
            "bottlenecks_count" to insights.bottlenecks.size,
            "recommendations_count" to insights.recommendations.size,
            "adjustments_count" to insights.dynamicAdjustments.size
        )
    }
}

/**
 * Manager for the periodic analysis system
 */
class AnalysisManager(private val context: Context) {
    
    /**
     * Start periodic analysis with custom configuration
     */
    fun startPeriodicAnalysis(
        periodHours: Long = 6L,
        analysisConfig: AnalysisConfig = AnalysisConfig()
    ) {
        PeriodicAnalysisWorker.schedulePeriodicAnalysis(context, periodHours)
    }
    
    /**
     * Stop periodic analysis
     */
    fun stopPeriodicAnalysis() {
        PeriodicAnalysisWorker.cancelPeriodicAnalysis(context)
    }
    
    /**
     * Trigger immediate analysis
     */
    fun triggerImmediateAnalysis(config: AnalysisConfig = AnalysisConfig()) {
        PeriodicAnalysisWorker.runOneTimeAnalysis(context)
    }
    
    /**
     * Get the status of the periodic analysis work
     */
    suspend fun getAnalysisWorkStatus(): WorkInfo? {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(PeriodicAnalysisWorker.WORK_NAME)
            .await()
            .firstOrNull()
    }
}

/**
 * Configuration for analysis parameters
 */
data class AnalysisConfig(
    val periodDays: Int = 7,
    val limitRuns: Int = 100,
    val enableTrendAnalysis: Boolean = true,
    val enableAnomalyDetection: Boolean = true,
    val enableBottleneckIdentification: Boolean = true
)