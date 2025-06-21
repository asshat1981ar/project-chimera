package com.xai.chimera.service

import com.xai.chimera.domain.*
import kotlinx.coroutines.delay
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/**
 * Enhanced conversation memory service for consciousness-aware contextual understanding
 */
class ConversationMemoryService {
    
    companion object {
        private const val MEMORY_DECAY_RATE = 0.98f
        private const val IMPORTANCE_THRESHOLD = 0.6f
        private const val SIMILARITY_THRESHOLD = 0.3f
    }
    
    /**
     * Find relevant memories from conversation history based on current context
     */
    suspend fun findRelevantMemories(
        currentContext: String,
        conversationHistory: List<DialogueEntry>,
        maxResults: Int = 5
    ): List<RelevantMemory> {
        delay(50) // Simulate processing time
        
        val memories = conversationHistory.map { entry ->
            val relevanceScore = calculateRelevanceScore(currentContext, entry)
            val importanceScore = calculateImportanceScore(entry)
            val temporalScore = calculateTemporalScore(entry.timestamp)
            
            RelevantMemory(
                dialogueEntry = entry,
                relevanceScore = relevanceScore,
                importanceScore = importanceScore,
                temporalScore = temporalScore,
                overallScore = (relevanceScore * 0.5f + importanceScore * 0.3f + temporalScore * 0.2f)
            )
        }
        
        return memories
            .filter { it.overallScore > SIMILARITY_THRESHOLD }
            .sortedByDescending { it.overallScore }
            .take(maxResults)
    }
    
    /**
     * Analyze relationship progression over time
     */
    suspend fun analyzeRelationshipProgression(
        playerId: String,
        conversationHistory: List<DialogueEntry>,
        timeWindowDays: Int = 30
    ): RelationshipProgressionInsight {
        delay(100)
        
        val recentHistory = filterRecentHistory(conversationHistory, timeWindowDays)
        
        return RelationshipProgressionInsight(
            intimacyProgression = calculateIntimacyProgression(recentHistory),
            trustEvolution = calculateTrustEvolution(recentHistory),
            conversationDepthTrend = calculateDepthTrend(recentHistory),
            emotionalSynchronization = calculateEmotionalSynchronization(recentHistory),
            communicationStyleEvolution = analyzeCommunicationStyleEvolution(recentHistory),
            sharedExperienceIndex = calculateSharedExperienceIndex(recentHistory)
        )
    }
    
    /**
     * Generate conversation context for enhanced dialogue generation
     */
    suspend fun generateConversationContext(
        currentPrompt: String,
        playerPersonality: ConversationPersonality,
        recentMemories: List<RelevantMemory>
    ): EnhancedConversationContext {
        delay(30)
        
        val topicContext = extractTopicContext(currentPrompt, recentMemories)
        val emotionalContext = extractEmotionalContext(recentMemories)
        val relationshipContext = extractRelationshipContext(recentMemories)
        
        return EnhancedConversationContext(
            primaryTopics = topicContext.primaryTopics,
            emotionalTone = emotionalContext.dominantTone,
            relationshipDepth = relationshipContext.currentDepth,
            conversationGoals = inferConversationGoals(currentPrompt, playerPersonality),
            contextualReferences = generateContextualReferences(recentMemories),
            suggestedResponseStyle = recommendResponseStyle(playerPersonality, emotionalContext)
        )
    }
    
    /**
     * Create memory consolidation recommendations
     */
    suspend fun consolidateMemories(
        conversationHistory: List<DialogueEntry>,
        memoryProfile: MemoryProfile
    ): MemoryConsolidationResult {
        delay(150)
        
        val importantMemories = identifyImportantMemories(conversationHistory, memoryProfile)
        val memoryPatterns = identifyMemoryPatterns(conversationHistory)
        val memoriesForLongTermStorage = selectMemoriesForLongTermStorage(importantMemories, memoryProfile)
        
        return MemoryConsolidationResult(
            importantMemories = importantMemories,
            identifiedPatterns = memoryPatterns,
            longTermMemories = memoriesForLongTermStorage,
            forgettableMemories = identifyForgettableMemories(conversationHistory, importantMemories),
            memoryStrengthUpdates = calculateMemoryStrengthUpdates(conversationHistory, memoryProfile)
        )
    }
    
    // Private helper methods
    
    private fun calculateRelevanceScore(currentContext: String, entry: DialogueEntry): Float {
        val contextWords = currentContext.lowercase().split("\\s+".toRegex())
        val entryWords = entry.text.lowercase().split("\\s+".toRegex())
        
        // Simple keyword matching (would be replaced with semantic similarity in production)
        val matches = contextWords.intersect(entryWords.toSet()).size
        val maxPossibleMatches = max(contextWords.size, entryWords.size)
        
        val keywordScore = if (maxPossibleMatches > 0) {
            matches.toFloat() / maxPossibleMatches
        } else 0f
        
        // Boost score for topic tag matches
        val topicBoost = entry.topicTags.any { tag ->
            currentContext.lowercase().contains(tag.lowercase())
        }.let { if (it) 0.3f else 0f }
        
        return min(1f, keywordScore + topicBoost)
    }
    
    private fun calculateImportanceScore(entry: DialogueEntry): Float {
        // Importance based on emotional intensity and conversation context
        val emotionalWeight = entry.emotions.values.maxOrNull() ?: 0.5f
        val contextWeight = entry.conversationContext.relationshipDepth
        val topicWeight = min(1f, entry.topicTags.size * 0.2f)
        
        return (emotionalWeight * 0.4f + contextWeight * 0.4f + topicWeight * 0.2f)
    }
    
    private fun calculateTemporalScore(timestamp: Long): Float {
        val daysSince = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60 * 24)
        return exp(-daysSince.toFloat() * 0.1f).toFloat() // Exponential decay
    }
    
    private fun filterRecentHistory(history: List<DialogueEntry>, days: Int): List<DialogueEntry> {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return history.filter { it.timestamp >= cutoffTime }
    }
    
    private fun calculateIntimacyProgression(history: List<DialogueEntry>): IntimacyProgression {
        if (history.isEmpty()) return IntimacyProgression.STABLE
        
        val intimacyScores = history.map { it.conversationContext.relationshipDepth }
        val trend = if (intimacyScores.size < 2) 0f 
                   else (intimacyScores.last() - intimacyScores.first()) / intimacyScores.size
        
        return when {
            trend > 0.05f -> IntimacyProgression.DEEPENING
            trend < -0.05f -> IntimacyProgression.DISTANCING
            else -> IntimacyProgression.STABLE
        }
    }
    
    private fun calculateTrustEvolution(history: List<DialogueEntry>): TrustEvolution {
        // Analyze trust indicators in conversation patterns
        val trustIndicators = history.map { entry ->
            val vulnerabilityShared = entry.emotions.containsKey("vulnerability") || 
                                    entry.emotions.containsKey("openness")
            val supportGiven = entry.text.lowercase().contains("support") ||
                             entry.text.lowercase().contains("understand")
            if (vulnerabilityShared || supportGiven) 0.1f else 0f
        }
        
        val trustGrowth = trustIndicators.sum() / history.size
        
        return when {
            trustGrowth > 0.05f -> TrustEvolution.BUILDING
            trustGrowth < 0.02f -> TrustEvolution.DECLINING
            else -> TrustEvolution.STABLE
        }
    }
    
    private fun calculateDepthTrend(history: List<DialogueEntry>): ConversationDepthTrend {
        val depthScores = history.map { entry ->
            when {
                entry.topicTags.any { it.contains("philosophy") || it.contains("meaning") } -> 1.0f
                entry.topicTags.any { it.contains("personal") || it.contains("emotional") } -> 0.7f
                entry.topicTags.any { it.contains("surface") || it.contains("casual") } -> 0.3f
                else -> 0.5f
            }
        }
        
        val trend = if (depthScores.size < 2) 0f
                   else (depthScores.takeLast(3).average() - depthScores.take(3).average()).toFloat()
        
        return when {
            trend > 0.1f -> ConversationDepthTrend.DEEPENING
            trend < -0.1f -> ConversationDepthTrend.BECOMING_SURFACE
            else -> ConversationDepthTrend.STABLE
        }
    }
    
    private fun calculateEmotionalSynchronization(history: List<DialogueEntry>): Float {
        // Measure how well emotions align between user and system over time
        return history.map { entry ->
            val emotionalComplexity = entry.emotions.size
            val emotionalIntensity = entry.emotions.values.sum()
            min(1f, emotionalComplexity * 0.1f + emotionalIntensity * 0.1f)
        }.average().toFloat()
    }
    
    private fun analyzeCommunicationStyleEvolution(
        history: List<DialogueEntry>
    ): CommunicationStyleEvolution {
        val recentStyle = analyzeRecentCommunicationStyle(history.takeLast(10))
        val earlierStyle = analyzeRecentCommunicationStyle(history.take(10))
        
        return CommunicationStyleEvolution(
            currentStyle = recentStyle,
            previousStyle = earlierStyle,
            evolutionDirection = determineEvolutionDirection(earlierStyle, recentStyle)
        )
    }
    
    private fun analyzeRecentCommunicationStyle(entries: List<DialogueEntry>): CommunicationStyle {
        if (entries.isEmpty()) return CommunicationStyle.BALANCED
        
        val avgLength = entries.map { it.text.length }.average()
        val questionRatio = entries.count { it.text.contains("?") }.toFloat() / entries.size
        val emotionalWords = entries.sumOf { entry ->
            listOf("feel", "emotion", "heart", "understand", "empathy").count { word ->
                entry.text.lowercase().contains(word)
            }
        }
        
        return when {
            emotionalWords > entries.size * 0.3 -> CommunicationStyle.EMPATHETIC
            avgLength > 100 && questionRatio < 0.2 -> CommunicationStyle.ANALYTICAL
            questionRatio > 0.4 -> CommunicationStyle.DIRECT
            avgLength < 50 -> CommunicationStyle.DIRECT
            else -> CommunicationStyle.BALANCED
        }
    }
    
    private fun calculateSharedExperienceIndex(history: List<DialogueEntry>): Float {
        val sharedExperiences = history.count { entry ->
            entry.text.lowercase().let { text ->
                text.contains("we") || text.contains("us") || text.contains("together") ||
                text.contains("shared") || text.contains("both")
            }
        }
        
        return if (history.isEmpty()) 0f else sharedExperiences.toFloat() / history.size
    }
    
    private fun extractTopicContext(
        prompt: String,
        memories: List<RelevantMemory>
    ): TopicContext {
        val currentTopics = extractTopicsFromText(prompt)
        val historicalTopics = memories.flatMap { it.dialogueEntry.topicTags }.distinct()
        
        return TopicContext(
            primaryTopics = currentTopics,
            relatedHistoricalTopics = historicalTopics,
            topicContinuity = calculateTopicContinuity(currentTopics, historicalTopics)
        )
    }
    
    private fun extractEmotionalContext(memories: List<RelevantMemory>): EmotionalContext {
        val recentEmotions = memories.flatMap { it.dialogueEntry.emotions.entries }
        val dominantEmotion = recentEmotions.groupBy { it.key }
            .mapValues { it.value.map { entry -> entry.value }.average().toFloat() }
            .maxByOrNull { it.value }
        
        return EmotionalContext(
            dominantTone = dominantEmotion?.key ?: "neutral",
            emotionalIntensity = dominantEmotion?.value ?: 0.5f,
            emotionalVariety = recentEmotions.map { it.key }.distinct().size
        )
    }
    
    private fun extractRelationshipContext(memories: List<RelevantMemory>): RelationshipContext {
        val avgDepth = memories.map { it.dialogueEntry.conversationContext.relationshipDepth }
            .average().toFloat()
        
        return RelationshipContext(
            currentDepth = avgDepth,
            progressionRate = calculateRelationshipProgressionRate(memories),
            intimacyMarkers = identifyIntimacyMarkers(memories)
        )
    }
    
    private fun inferConversationGoals(
        prompt: String,
        personality: ConversationPersonality
    ): List<String> {
        val goals = mutableListOf<String>()
        
        val lowerPrompt = prompt.lowercase()
        when {
            lowerPrompt.contains("help") || lowerPrompt.contains("advice") -> goals.add("problem_solving")
            lowerPrompt.contains("feel") || lowerPrompt.contains("emotion") -> goals.add("emotional_support")
            lowerPrompt.contains("?") -> goals.add("information_seeking")
            lowerPrompt.contains("story") || lowerPrompt.contains("tell") -> goals.add("narrative_sharing")
            else -> goals.add("general_conversation")
        }
        
        // Add personality-based goals
        when (personality.communicationStyle) {
            CommunicationStyle.EMPATHETIC -> goals.add("emotional_connection")
            CommunicationStyle.ANALYTICAL -> goals.add("deep_understanding")
            CommunicationStyle.DIRECT -> goals.add("efficient_communication")
            else -> goals.add("balanced_interaction")
        }
        
        return goals.distinct()
    }
    
    private fun generateContextualReferences(memories: List<RelevantMemory>): List<String> {
        return memories.take(3).map { memory ->
            "Previously, you mentioned: \"${memory.dialogueEntry.text.take(50)}...\""
        }
    }
    
    private fun recommendResponseStyle(
        personality: ConversationPersonality,
        emotionalContext: EmotionalContext
    ): ResponseStyle {
        return when {
            emotionalContext.dominantTone in listOf("sadness", "anxiety", "fear") -> ResponseStyle.GENTLE_SUPPORTIVE
            emotionalContext.dominantTone in listOf("excitement", "joy") -> ResponseStyle.ENTHUSIASTIC
            personality.communicationStyle == CommunicationStyle.ANALYTICAL -> ResponseStyle.DETAILED_ANALYTICAL
            personality.communicationStyle == CommunicationStyle.EMPATHETIC -> ResponseStyle.WARM_EMPATHETIC
            else -> ResponseStyle.BALANCED_CONVERSATIONAL
        }
    }
    
    private fun identifyImportantMemories(
        history: List<DialogueEntry>,
        memoryProfile: MemoryProfile
    ): List<ImportantMemory> {
        return history.mapNotNull { entry ->
            val importanceScore = calculateImportanceScore(entry)
            if (importanceScore >= IMPORTANCE_THRESHOLD) {
                ImportantMemory(
                    dialogueEntry = entry,
                    importanceScore = importanceScore,
                    memoryType = classifyMemoryType(entry),
                    emotionalSignificance = entry.emotions.values.maxOrNull() ?: 0f
                )
            } else null
        }
    }
    
    private fun identifyMemoryPatterns(history: List<DialogueEntry>): List<MemoryPattern> {
        // Analyze patterns in conversation topics, emotions, and timing
        val topicPatterns = analyzeTopicPatterns(history)
        val emotionalPatterns = analyzeEmotionalPatterns(history)
        val temporalPatterns = analyzeTemporalPatterns(history)
        
        return listOf(topicPatterns, emotionalPatterns, temporalPatterns).flatten()
    }
    
    private fun selectMemoriesForLongTermStorage(
        importantMemories: List<ImportantMemory>,
        memoryProfile: MemoryProfile
    ): List<LongTermMemory> {
        return importantMemories
            .sortedByDescending { it.importanceScore }
            .take((importantMemories.size * memoryProfile.memoryRetentionRate).toInt())
            .map { memory ->
                LongTermMemory(
                    originalMemory = memory,
                    consolidationStrength = calculateConsolidationStrength(memory, memoryProfile),
                    associativeLinks = findAssociativeLinks(memory, importantMemories)
                )
            }
    }
    
    private fun identifyForgettableMemories(
        history: List<DialogueEntry>,
        importantMemories: List<ImportantMemory>
    ): List<DialogueEntry> {
        val importantIds = importantMemories.map { it.dialogueEntry.id }.toSet()
        return history.filter { entry ->
            entry.id !in importantIds && 
            calculateImportanceScore(entry) < IMPORTANCE_THRESHOLD &&
            isOldEnoughToForget(entry.timestamp)
        }
    }
    
    private fun calculateMemoryStrengthUpdates(
        history: List<DialogueEntry>,
        memoryProfile: MemoryProfile
    ): Map<String, Float> {
        return history.associate { entry ->
            val currentStrength = 1.0f // Would be retrieved from storage
            val newStrength = currentStrength * MEMORY_DECAY_RATE + 
                            (if (isRecentlyReferenced(entry)) 0.1f else 0f)
            entry.id to newStrength.coerceIn(0f, 1f)
        }
    }
    
    // Additional helper methods
    private fun extractTopicsFromText(text: String): List<String> {
        // Simple topic extraction (would use NLP in production)
        val topics = mutableListOf<String>()
        val lowerText = text.lowercase()
        
        val topicKeywords = mapOf(
            "work" to listOf("job", "career", "work", "office", "boss"),
            "relationships" to listOf("friend", "family", "love", "relationship", "partner"),
            "emotions" to listOf("feel", "emotion", "sad", "happy", "angry", "excited"),
            "goals" to listOf("goal", "dream", "ambition", "future", "plan"),
            "learning" to listOf("learn", "study", "book", "knowledge", "skill")
        )
        
        topicKeywords.forEach { (topic, keywords) ->
            if (keywords.any { lowerText.contains(it) }) {
                topics.add(topic)
            }
        }
        
        return topics.ifEmpty { listOf("general") }
    }
    
    private fun calculateTopicContinuity(current: List<String>, historical: List<String>): Float {
        val overlap = current.intersect(historical.toSet()).size
        val total = (current + historical).distinct().size
        return if (total > 0) overlap.toFloat() / total else 0f
    }
    
    private fun calculateRelationshipProgressionRate(memories: List<RelevantMemory>): Float {
        if (memories.size < 2) return 0f
        
        val depths = memories.map { it.dialogueEntry.conversationContext.relationshipDepth }
        return ((depths.last() - depths.first()) / memories.size).toFloat()
    }
    
    private fun identifyIntimacyMarkers(memories: List<RelevantMemory>): List<String> {
        return memories.flatMap { memory ->
            val markers = mutableListOf<String>()
            val text = memory.dialogueEntry.text.lowercase()
            
            if (text.contains("personal") || text.contains("private")) markers.add("personal_sharing")
            if (text.contains("trust") || text.contains("confidence")) markers.add("trust_expression")
            if (text.contains("vulnerable") || text.contains("difficult")) markers.add("vulnerability")
            if (text.contains("appreciate") || text.contains("grateful")) markers.add("appreciation")
            
            markers
        }.distinct()
    }
    
    private fun determineEvolutionDirection(
        previous: CommunicationStyle,
        current: CommunicationStyle
    ): EvolutionDirection {
        return when {
            previous == current -> EvolutionDirection.STABLE
            isMoreEmotional(current, previous) -> EvolutionDirection.MORE_EMOTIONAL
            isMoreAnalytical(current, previous) -> EvolutionDirection.MORE_ANALYTICAL
            isMoreDirect(current, previous) -> EvolutionDirection.MORE_DIRECT
            else -> EvolutionDirection.ADAPTING
        }
    }
    
    private fun classifyMemoryType(entry: DialogueEntry): MemoryType {
        return when {
            entry.emotions.values.maxOrNull() ?: 0f > 0.7f -> MemoryType.EMOTIONAL_SIGNIFICANT
            entry.topicTags.contains("personal") -> MemoryType.PERSONAL_DISCLOSURE
            entry.topicTags.contains("achievement") -> MemoryType.MILESTONE
            entry.conversationContext.relationshipDepth > 0.7f -> MemoryType.INTIMATE_CONVERSATION
            else -> MemoryType.GENERAL_CONVERSATION
        }
    }
    
    private fun analyzeTopicPatterns(history: List<DialogueEntry>): List<MemoryPattern> {
        // Implementation for topic pattern analysis
        return emptyList() // Placeholder
    }
    
    private fun analyzeEmotionalPatterns(history: List<DialogueEntry>): List<MemoryPattern> {
        // Implementation for emotional pattern analysis
        return emptyList() // Placeholder
    }
    
    private fun analyzeTemporalPatterns(history: List<DialogueEntry>): List<MemoryPattern> {
        // Implementation for temporal pattern analysis
        return emptyList() // Placeholder
    }
    
    private fun calculateConsolidationStrength(
        memory: ImportantMemory,
        profile: MemoryProfile
    ): Float {
        return memory.importanceScore * profile.memoryRetentionRate
    }
    
    private fun findAssociativeLinks(
        memory: ImportantMemory,
        allMemories: List<ImportantMemory>
    ): List<String> {
        // Find memories with similar topics or emotions
        return allMemories.filter { other ->
            other.dialogueEntry.id != memory.dialogueEntry.id &&
            (memory.dialogueEntry.topicTags.intersect(other.dialogueEntry.topicTags.toSet()).isNotEmpty() ||
             memory.dialogueEntry.emotions.keys.intersect(other.dialogueEntry.emotions.keys).isNotEmpty())
        }.map { it.dialogueEntry.id }
    }
    
    private fun isOldEnoughToForget(timestamp: Long): Boolean {
        val daysSince = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60 * 24)
        return daysSince > 30 // Forget memories older than 30 days if not important
    }
    
    private fun isRecentlyReferenced(entry: DialogueEntry): Boolean {
        // Check if this memory was referenced in recent conversations
        return false // Placeholder implementation
    }
    
    private fun isMoreEmotional(current: CommunicationStyle, previous: CommunicationStyle): Boolean {
        val emotionalOrder = listOf(CommunicationStyle.ANALYTICAL, CommunicationStyle.DIRECT, 
                                  CommunicationStyle.BALANCED, CommunicationStyle.DIPLOMATIC, 
                                  CommunicationStyle.EMPATHETIC)
        return emotionalOrder.indexOf(current) > emotionalOrder.indexOf(previous)
    }
    
    private fun isMoreAnalytical(current: CommunicationStyle, previous: CommunicationStyle): Boolean {
        return current == CommunicationStyle.ANALYTICAL && previous != CommunicationStyle.ANALYTICAL
    }
    
    private fun isMoreDirect(current: CommunicationStyle, previous: CommunicationStyle): Boolean {
        return current == CommunicationStyle.DIRECT && previous in listOf(CommunicationStyle.DIPLOMATIC, CommunicationStyle.BALANCED)
    }
}

// Supporting data classes for conversation memory system

data class RelevantMemory(
    val dialogueEntry: DialogueEntry,
    val relevanceScore: Float,
    val importanceScore: Float,
    val temporalScore: Float,
    val overallScore: Float
)

data class RelationshipProgressionInsight(
    val intimacyProgression: IntimacyProgression,
    val trustEvolution: TrustEvolution,
    val conversationDepthTrend: ConversationDepthTrend,
    val emotionalSynchronization: Float,
    val communicationStyleEvolution: CommunicationStyleEvolution,
    val sharedExperienceIndex: Float
)

data class EnhancedConversationContext(
    val primaryTopics: List<String>,
    val emotionalTone: String,
    val relationshipDepth: Float,
    val conversationGoals: List<String>,
    val contextualReferences: List<String>,
    val suggestedResponseStyle: ResponseStyle
)

data class MemoryConsolidationResult(
    val importantMemories: List<ImportantMemory>,
    val identifiedPatterns: List<MemoryPattern>,
    val longTermMemories: List<LongTermMemory>,
    val forgettableMemories: List<DialogueEntry>,
    val memoryStrengthUpdates: Map<String, Float>
)

data class TopicContext(
    val primaryTopics: List<String>,
    val relatedHistoricalTopics: List<String>,
    val topicContinuity: Float
)

data class EmotionalContext(
    val dominantTone: String,
    val emotionalIntensity: Float,
    val emotionalVariety: Int
)

data class RelationshipContext(
    val currentDepth: Float,
    val progressionRate: Float,
    val intimacyMarkers: List<String>
)

data class CommunicationStyleEvolution(
    val currentStyle: CommunicationStyle,
    val previousStyle: CommunicationStyle,
    val evolutionDirection: EvolutionDirection
)

data class ImportantMemory(
    val dialogueEntry: DialogueEntry,
    val importanceScore: Float,
    val memoryType: MemoryType,
    val emotionalSignificance: Float
)

data class LongTermMemory(
    val originalMemory: ImportantMemory,
    val consolidationStrength: Float,
    val associativeLinks: List<String>
)

data class MemoryPattern(
    val patternType: String,
    val description: String,
    val strength: Float,
    val examples: List<String>
)

enum class IntimacyProgression {
    DEEPENING, STABLE, DISTANCING
}

enum class TrustEvolution {
    BUILDING, STABLE, DECLINING
}

enum class ConversationDepthTrend {
    DEEPENING, STABLE, BECOMING_SURFACE
}

enum class EvolutionDirection {
    STABLE, MORE_EMOTIONAL, MORE_ANALYTICAL, MORE_DIRECT, ADAPTING
}

enum class ResponseStyle {
    GENTLE_SUPPORTIVE, ENTHUSIASTIC, DETAILED_ANALYTICAL, WARM_EMPATHETIC, BALANCED_CONVERSATIONAL
}

enum class MemoryType {
    EMOTIONAL_SIGNIFICANT, PERSONAL_DISCLOSURE, MILESTONE, INTIMATE_CONVERSATION, GENERAL_CONVERSATION
}