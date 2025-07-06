# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep SystemArchetypeEngine classes for runtime reflection
-keep class com.chimera.core.archetypes.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class **_HiltComponents$** { *; }

# Keep Jetpack Compose
-keep class androidx.compose.** { *; }

# Keep data classes used in UI
-keep class com.chimera.ui.**$* { *; }

# Keep emotion and archetype enums
-keepclassmembers enum com.chimera.core.archetypes.** { *; }