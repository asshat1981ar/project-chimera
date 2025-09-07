package com.xai.chimera.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for tracking self-optimization metrics for automated analysis
 */
@Entity(tableName = "self_opt_metrics")
data class SelfOptMetrics(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val runId: String,
    val processingTimeMs: Long,
    val fetchErrors: Int,
    val chunkSize: Int,
    val memoryUsageMb: Float,
    val successRate: Float,
    val emotionProcessingTimeMs: Long,
    val consciousnessUpdateTimeMs: Long,
    val apiResponseTimeMs: Long,
    val playerEmotionVariance: Float,
    val conversationComplexity: Float,
    val systemLoad: Float,
    val additionalMetrics: Map<String, Float> = emptyMap()
)

/**
 * Data class for trend analysis results
 */
data class TrendAnalysis(
    val metricName: String,
    val trend: TrendDirection,
    val changeRate: Float,
    val confidence: Float,
    val periodDays: Int
)

/**
 * Data class for anomaly detection results
 */
data class AnomalyDetection(
    val metricName: String,
    val anomalyType: AnomalyType,
    val severity: AnomalySeverity,
    val currentValue: Float,
    val expectedRange: Pair<Float, Float>,
    val timestamp: Long
)

/**
 * Data class for bottleneck identification
 */
data class BottleneckAnalysis(
    val componentName: String,
    val bottleneckType: BottleneckType,
    val impact: Float,
    val suggestedAction: String,
    val affectedMetrics: List<String>
)

/**
 * Comprehensive analysis insights
 */
data class AnalysisInsights(
    val analysisTimestamp: Long,
    val periodAnalyzed: Pair<Long, Long>,
    val totalRuns: Int,
    val trends: List<TrendAnalysis>,
    val anomalies: List<AnomalyDetection>,
    val bottlenecks: List<BottleneckAnalysis>,
    val overallPerformanceScore: Float,
    val recommendations: List<String>,
    val dynamicAdjustments: Map<String, Float>
)

enum class TrendDirection {
    IMPROVING, DEGRADING, STABLE, VOLATILE
}

enum class AnomalyType {
    SPIKE, DIP, PATTERN_BREAK, THRESHOLD_EXCEEDED
}

enum class AnomalySeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class BottleneckType {
    PROCESSING_TIME, MEMORY_USAGE, API_LATENCY, EMOTION_PROCESSING, CONSCIOUSNESS_UPDATE
}