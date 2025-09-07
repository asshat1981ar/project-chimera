package com.xai.chimera.service

import com.xai.chimera.dao.SelfOptMetricsDao
import com.xai.chimera.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.*

/**
 * Service for automated analysis and insight generation from self-optimization metrics
 */
class AnalysisService(
    private val selfOptMetricsDao: SelfOptMetricsDao
) {
    
    companion object {
        private const val DEFAULT_ANALYSIS_PERIOD_DAYS = 7
        private const val MIN_SAMPLES_FOR_TREND = 5
        private const val ANOMALY_THRESHOLD_MULTIPLIER = 2.0f
        private const val BOTTLENECK_THRESHOLD = 0.8f
    }
    
    /**
     * Perform comprehensive analysis of recent optimization metrics
     */
    suspend fun performAnalysis(
        periodDays: Int = DEFAULT_ANALYSIS_PERIOD_DAYS,
        limitRuns: Int = 100
    ): AnalysisInsights = withContext(Dispatchers.IO) {
        
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (periodDays * 24 * 60 * 60 * 1000L)
        
        // Get recent metrics for analysis
        val recentMetrics = selfOptMetricsDao.getMetricsInRange(startTime, endTime)
            .take(limitRuns)
        
        if (recentMetrics.isEmpty()) {
            return@withContext createEmptyAnalysis(startTime, endTime)
        }
        
        // Perform different types of analysis
        val trends = computeTrends(recentMetrics, periodDays)
        val anomalies = detectAnomalies(recentMetrics)
        val bottlenecks = identifyBottlenecks(recentMetrics)
        val performanceScore = calculateOverallPerformanceScore(recentMetrics)
        val recommendations = generateRecommendations(trends, anomalies, bottlenecks)
        val dynamicAdjustments = computeDynamicAdjustments(trends, anomalies)
        
        AnalysisInsights(
            analysisTimestamp = System.currentTimeMillis(),
            periodAnalyzed = Pair(startTime, endTime),
            totalRuns = recentMetrics.size,
            trends = trends,
            anomalies = anomalies,
            bottlenecks = bottlenecks,
            overallPerformanceScore = performanceScore,
            recommendations = recommendations,
            dynamicAdjustments = dynamicAdjustments
        )
    }
    
    /**
     * Compute trends for various metrics
     */
    private fun computeTrends(metrics: List<SelfOptMetrics>, periodDays: Int): List<TrendAnalysis> {
        if (metrics.size < MIN_SAMPLES_FOR_TREND) return emptyList()
        
        val trends = mutableListOf<TrendAnalysis>()
        
        // Processing time trend
        trends.add(analyzeTrend(
            "processing_time",
            metrics.map { it.processingTimeMs.toFloat() },
            periodDays
        ))
        
        // Fetch errors trend
        trends.add(analyzeTrend(
            "fetch_errors",
            metrics.map { it.fetchErrors.toFloat() },
            periodDays
        ))
        
        // Chunk size variance trend
        trends.add(analyzeChunkSizeVariance(metrics, periodDays))
        
        // Memory usage trend
        trends.add(analyzeTrend(
            "memory_usage",
            metrics.map { it.memoryUsageMb },
            periodDays
        ))
        
        // Success rate trend
        trends.add(analyzeTrend(
            "success_rate",
            metrics.map { it.successRate },
            periodDays
        ))
        
        // API response time trend
        trends.add(analyzeTrend(
            "api_response_time",
            metrics.map { it.apiResponseTimeMs.toFloat() },
            periodDays
        ))
        
        return trends
    }
    
    /**
     * Analyze trend for a specific metric
     */
    private fun analyzeTrend(
        metricName: String,
        values: List<Float>,
        periodDays: Int
    ): TrendAnalysis {
        val n = values.size
        val x = (0 until n).map { it.toFloat() }
        val y = values
        
        // Calculate linear regression
        val meanX = x.average().toFloat()
        val meanY = y.average()
        
        val numerator = x.zip(y) { xi, yi -> (xi - meanX) * (yi - meanY) }.sum()
        val denominator = x.map { (it - meanX).pow(2) }.sum()
        
        val slope = if (denominator != 0f) numerator / denominator else 0f
        val confidence = calculateTrendConfidence(x, y, slope, meanX, meanY)
        
        val trend = when {
            abs(slope) < 0.1f -> TrendDirection.STABLE
            slope > 0 && metricName != "fetch_errors" -> TrendDirection.IMPROVING
            slope > 0 && metricName == "fetch_errors" -> TrendDirection.DEGRADING
            slope < 0 && metricName != "fetch_errors" -> TrendDirection.DEGRADING
            else -> TrendDirection.IMPROVING
        }
        
        // Check for volatility
        val volatility = calculateVolatility(values)
        val finalTrend = if (volatility > 0.3f) TrendDirection.VOLATILE else trend
        
        return TrendAnalysis(
            metricName = metricName,
            trend = finalTrend,
            changeRate = slope,
            confidence = confidence,
            periodDays = periodDays
        )
    }
    
    /**
     * Analyze chunk size variance as a special case
     */
    private fun analyzeChunkSizeVariance(metrics: List<SelfOptMetrics>, periodDays: Int): TrendAnalysis {
        val chunkSizes = metrics.map { it.chunkSize.toFloat() }
        val variance = calculateVariance(chunkSizes)
        val variances = metrics.windowed(5) { window ->
            calculateVariance(window.map { it.chunkSize.toFloat() })
        }
        
        return analyzeTrend("chunk_size_variance", variances, periodDays)
    }
    
    /**
     * Detect anomalies in the metrics
     */
    private fun detectAnomalies(metrics: List<SelfOptMetrics>): List<AnomalyDetection> {
        val anomalies = mutableListOf<AnomalyDetection>()
        
        // Analyze processing time anomalies
        anomalies.addAll(detectMetricAnomalies(
            "processing_time",
            metrics.map { it.processingTimeMs.toFloat() },
            metrics.map { it.timestamp }
        ))
        
        // Analyze memory usage anomalies
        anomalies.addAll(detectMetricAnomalies(
            "memory_usage",
            metrics.map { it.memoryUsageMb },
            metrics.map { it.timestamp }
        ))
        
        // Analyze success rate anomalies
        anomalies.addAll(detectMetricAnomalies(
            "success_rate",
            metrics.map { it.successRate },
            metrics.map { it.timestamp }
        ))
        
        return anomalies
    }
    
    /**
     * Detect anomalies for a specific metric
     */
    private fun detectMetricAnomalies(
        metricName: String,
        values: List<Float>,
        timestamps: List<Long>
    ): List<AnomalyDetection> {
        if (values.size < 3) return emptyList()
        
        val mean = values.average().toFloat()
        val stdDev = calculateStandardDeviation(values, mean)
        val threshold = stdDev * ANOMALY_THRESHOLD_MULTIPLIER
        
        return values.zip(timestamps) { value, timestamp ->
            val deviation = abs(value - mean)
            when {
                deviation > threshold * 2 -> AnomalyDetection(
                    metricName = metricName,
                    anomalyType = if (value > mean) AnomalyType.SPIKE else AnomalyType.DIP,
                    severity = AnomalySeverity.CRITICAL,
                    currentValue = value,
                    expectedRange = Pair(mean - threshold, mean + threshold),
                    timestamp = timestamp
                )
                deviation > threshold -> AnomalyDetection(
                    metricName = metricName,
                    anomalyType = if (value > mean) AnomalyType.SPIKE else AnomalyType.DIP,
                    severity = AnomalySeverity.HIGH,
                    currentValue = value,
                    expectedRange = Pair(mean - threshold, mean + threshold),
                    timestamp = timestamp
                )
                else -> null
            }
        }.filterNotNull()
    }
    
    /**
     * Identify system bottlenecks
     */
    private fun identifyBottlenecks(metrics: List<SelfOptMetrics>): List<BottleneckAnalysis> {
        val bottlenecks = mutableListOf<BottleneckAnalysis>()
        
        val avgProcessingTime = metrics.map { it.processingTimeMs }.average().toFloat()
        val avgApiTime = metrics.map { it.apiResponseTimeMs }.average().toFloat()
        val avgEmotionTime = metrics.map { it.emotionProcessingTimeMs }.average().toFloat()
        val avgConsciousnessTime = metrics.map { it.consciousnessUpdateTimeMs }.average().toFloat()
        val avgMemoryUsage = metrics.map { it.memoryUsageMb }.average()
        
        // Check API response time bottleneck
        if (avgApiTime / avgProcessingTime > BOTTLENECK_THRESHOLD) {
            bottlenecks.add(BottleneckAnalysis(
                componentName = "API Response",
                bottleneckType = BottleneckType.API_LATENCY,
                impact = (avgApiTime / avgProcessingTime).coerceAtMost(1f),
                suggestedAction = "Consider implementing request caching or optimizing API calls",
                affectedMetrics = listOf("processing_time", "api_response_time")
            ))
        }
        
        // Check memory usage bottleneck
        if (avgMemoryUsage > 80f) {
            bottlenecks.add(BottleneckAnalysis(
                componentName = "Memory Management",
                bottleneckType = BottleneckType.MEMORY_USAGE,
                impact = (avgMemoryUsage / 100f).coerceAtMost(1f),
                suggestedAction = "Implement memory optimization and garbage collection tuning",
                affectedMetrics = listOf("memory_usage", "processing_time")
            ))
        }
        
        // Check emotion processing bottleneck
        if (avgEmotionTime / avgProcessingTime > 0.3f) {
            bottlenecks.add(BottleneckAnalysis(
                componentName = "Emotion Processing",
                bottleneckType = BottleneckType.EMOTION_PROCESSING,
                impact = (avgEmotionTime / avgProcessingTime).coerceAtMost(1f),
                suggestedAction = "Optimize emotion analysis algorithms or consider parallel processing",
                affectedMetrics = listOf("emotion_processing_time", "processing_time")
            ))
        }
        
        return bottlenecks
    }
    
    /**
     * Calculate overall performance score
     */
    private fun calculateOverallPerformanceScore(metrics: List<SelfOptMetrics>): Float {
        val avgSuccessRate = metrics.map { it.successRate }.average().toFloat()
        val avgProcessingTime = metrics.map { it.processingTimeMs }.average()
        val avgMemoryUsage = metrics.map { it.memoryUsageMb }.average()
        
        // Normalize scores (higher is better)
        val successScore = avgSuccessRate
        val timeScore = (1000f / avgProcessingTime.toFloat()).coerceAtMost(1f)
        val memoryScore = ((100f - avgMemoryUsage) / 100f).coerceAtLeast(0f)
        
        return (successScore * 0.5f + timeScore * 0.3f + memoryScore * 0.2f).coerceIn(0f, 1f)
    }
    
    /**
     * Generate actionable recommendations
     */
    private fun generateRecommendations(
        trends: List<TrendAnalysis>,
        anomalies: List<AnomalyDetection>,
        bottlenecks: List<BottleneckAnalysis>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Trend-based recommendations
        trends.forEach { trend ->
            when (trend.trend) {
                TrendDirection.DEGRADING -> {
                    recommendations.add("${trend.metricName} showing degrading trend - investigate and optimize")
                }
                TrendDirection.VOLATILE -> {
                    recommendations.add("${trend.metricName} is volatile - implement stabilization measures")
                }
                else -> { /* No action needed for improving/stable trends */ }
            }
        }
        
        // Anomaly-based recommendations
        val criticalAnomalies = anomalies.filter { it.severity == AnomalySeverity.CRITICAL }
        if (criticalAnomalies.isNotEmpty()) {
            recommendations.add("Critical anomalies detected - immediate investigation required")
        }
        
        // Bottleneck-based recommendations
        bottlenecks.forEach { bottleneck ->
            recommendations.add(bottleneck.suggestedAction)
        }
        
        return recommendations
    }
    
    /**
     * Compute dynamic adjustments based on analysis
     */
    private fun computeDynamicAdjustments(
        trends: List<TrendAnalysis>,
        anomalies: List<AnomalyDetection>
    ): Map<String, Float> {
        val adjustments = mutableMapOf<String, Float>()
        
        // Adjust chunk size based on variance trend
        val chunkVarianceTrend = trends.find { it.metricName == "chunk_size_variance" }
        chunkVarianceTrend?.let { trend ->
            when (trend.trend) {
                TrendDirection.DEGRADING -> adjustments["chunk_size_adjustment"] = -0.1f
                TrendDirection.VOLATILE -> adjustments["chunk_size_adjustment"] = -0.05f
                else -> { /* No adjustment needed */ }
            }
        }
        
        // Adjust processing timeout based on processing time trends
        val processingTimeTrend = trends.find { it.metricName == "processing_time" }
        processingTimeTrend?.let { trend ->
            when (trend.trend) {
                TrendDirection.DEGRADING -> adjustments["timeout_multiplier"] = 1.2f
                TrendDirection.IMPROVING -> adjustments["timeout_multiplier"] = 0.9f
                else -> { /* No adjustment needed */ }
            }
        }
        
        return adjustments
    }
    
    // Utility functions
    private fun calculateTrendConfidence(
        x: List<Float>,
        y: List<Float>,
        slope: Float,
        meanX: Float,
        meanY: Float
    ): Float {
        val predictions = x.map { meanY + slope * (it - meanX) }
        val actualVariance = y.zip(predictions) { actual, predicted ->
            (actual - predicted).pow(2)
        }.average().toFloat()
        
        val totalVariance = y.map { (it - meanY).pow(2) }.average().toFloat()
        
        return if (totalVariance > 0) {
            (1 - actualVariance / totalVariance).coerceIn(0f, 1f)
        } else 1f
    }
    
    private fun calculateVolatility(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val mean = values.average().toFloat()
        return calculateStandardDeviation(values, mean) / mean
    }
    
    private fun calculateVariance(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val mean = values.average().toFloat()
        return values.map { (it - mean).pow(2) }.average().toFloat()
    }
    
    private fun calculateStandardDeviation(values: List<Float>, mean: Float): Float {
        if (values.size < 2) return 0f
        val variance = values.map { (it - mean).pow(2) }.average().toFloat()
        return sqrt(variance)
    }
    
    private fun createEmptyAnalysis(startTime: Long, endTime: Long): AnalysisInsights {
        return AnalysisInsights(
            analysisTimestamp = System.currentTimeMillis(),
            periodAnalyzed = Pair(startTime, endTime),
            totalRuns = 0,
            trends = emptyList(),
            anomalies = emptyList(),
            bottlenecks = emptyList(),
            overallPerformanceScore = 0f,
            recommendations = listOf("No metrics available for analysis"),
            dynamicAdjustments = emptyMap()
        )
    }
}