package com.xai.chimera.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xai.chimera.domain.*

/**
 * Type converters for Room database to handle complex consciousness data types
 */
class Converters {
    
    private val gson = Gson()
    
    // Map<String, Float> converters for emotions and interests
    @TypeConverter
    fun fromStringFloatMap(value: Map<String, Float>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringFloatMap(value: String): Map<String, Float> {
        val type = object : TypeToken<Map<String, Float>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }
    
    // List<String> converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
    
    // DialogueEntry list converters
    @TypeConverter
    fun fromDialogueEntryList(value: List<DialogueEntry>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toDialogueEntryList(value: String): List<DialogueEntry> {
        val type = object : TypeToken<List<DialogueEntry>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
    
    // ConversationPersonality converters
    @TypeConverter
    fun fromConversationPersonality(value: ConversationPersonality?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toConversationPersonality(value: String): ConversationPersonality {
        return gson.fromJson(value, ConversationPersonality::class.java) ?: ConversationPersonality()
    }
    
    // EmotionalProfile converters
    @TypeConverter
    fun fromEmotionalProfile(value: EmotionalProfile?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toEmotionalProfile(value: String): EmotionalProfile {
        return gson.fromJson(value, EmotionalProfile::class.java) ?: EmotionalProfile()
    }
    
    // MemoryProfile converters
    @TypeConverter
    fun fromMemoryProfile(value: MemoryProfile?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toMemoryProfile(value: String): MemoryProfile {
        return gson.fromJson(value, MemoryProfile::class.java) ?: MemoryProfile()
    }
    
    // ConversationContext converters
    @TypeConverter
    fun fromConversationContext(value: ConversationContext?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toConversationContext(value: String): ConversationContext {
        return gson.fromJson(value, ConversationContext::class.java) ?: ConversationContext()
    }
    
    // Map<String, List<Float>> converters for emotional patterns
    @TypeConverter
    fun fromStringListFloatMap(value: Map<String, List<Float>>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringListFloatMap(value: String): Map<String, List<Float>> {
        val type = object : TypeToken<Map<String, List<Float>>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }
    
    // Enum converters
    @TypeConverter
    fun fromCommunicationStyle(value: CommunicationStyle?): String {
        return value?.name ?: CommunicationStyle.BALANCED.name
    }
    
    @TypeConverter
    fun toCommunicationStyle(value: String): CommunicationStyle {
        return try {
            CommunicationStyle.valueOf(value)
        } catch (e: IllegalArgumentException) {
            CommunicationStyle.BALANCED
        }
    }
    
    @TypeConverter
    fun fromHumorStyle(value: HumorStyle?): String {
        return value?.name ?: HumorStyle.MILD.name
    }
    
    @TypeConverter
    fun toHumorStyle(value: String): HumorStyle {
        return try {
            HumorStyle.valueOf(value)
        } catch (e: IllegalArgumentException) {
            HumorStyle.MILD
        }
    }
    
    @TypeConverter
    fun fromConversationDepth(value: ConversationDepth?): String {
        return value?.name ?: ConversationDepth.MEDIUM.name
    }
    
    @TypeConverter
    fun toConversationDepth(value: String): ConversationDepth {
        return try {
            ConversationDepth.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ConversationDepth.MEDIUM
        }
    }
    
    @TypeConverter
    fun fromLearningStyle(value: LearningStyle?): String {
        return value?.name ?: LearningStyle.BALANCED.name
    }
    
    @TypeConverter
    fun toLearningStyle(value: String): LearningStyle {
        return try {
            LearningStyle.valueOf(value)
        } catch (e: IllegalArgumentException) {
            LearningStyle.BALANCED
        }
    }
}