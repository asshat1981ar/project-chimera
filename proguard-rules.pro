# Chimera: Ashes of the Hollow King - ProGuard Rules

# Keep Room entities (needed for reflection-based serialization)
-keep class com.chimera.database.entity.** { *; }

# Keep RelationshipArchetypeEngine and its inner classes
-keep class com.chimera.database.engine.** { *; }

# Keep domain models (used across module boundaries)
-keep class com.chimera.model.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class **_HiltComponents$** { *; }

# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.chimera.model.**$$serializer { *; }
-keepclassmembers class com.chimera.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.chimera.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep enums
-keepclassmembers enum com.chimera.** { *; }
