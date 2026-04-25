# Memory Performance Baseline

## Target Metrics

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| Initial Heap Size | <50MB | adb shell dumpsys meminfo |
| Steady State Memory | <100MB | Profiler / dumpsys |
| Peak Memory (game load) | <200MB | Stress test scenarios |
| Native Memory | <50MB | Profiler Native tab |
| Graphics Memory | <30MB | GPU Profiler |

## Measurement Setup

### Memory Profiling Commands

```bash
# Quick memory snapshot
adb shell dumpsys meminfo com.chimera.ashes

# Detailed memory info with PSS breakdown
adb shell dumpsys meminfo --package com.chimera.ashes

# Trigger GC and measure
adb shell am send-trim-memory com.chimera.ashes RUNNING_LOW
adb shell dumpsys meminfo com.chimera.ashes

# Heap dump for leak analysis
adb shell am dumpheap com.chimera.ashes /sdcard/chimera.hprof
adb pull /sdcard/chimera.hprof ./memory-analysis/
# Open in Android Studio Profiler or MAT
```

### Android Studio Profiler Setup

1. Run app in debug mode
2. Open Profiler tab
3. Select "Memory" tracker
4. Record heap dumps during:
   - App startup
   - Scene transitions
   - After extended gameplay sessions

## Baseline Measurements

### Device: [Device Name]
### Date: [Measurement Date]

| Scenario | Java Heap | Native Heap | Graphics | Total PSS |
|----------|-----------|-------------|----------|-----------|
| App Launch | - | - | - | - |
| Main Menu | - | - | - | - |
| Game Loaded | - | - | - | - |
| Combat Scene | - | - | - | - |
| After 10 min | - | - | - | - |

## Memory Budget Allocation

| Component | Budget | Notes |
|-----------|--------|-------|
| Core App | 20MB | DI, state, viewmodels |
| UI/Compose | 15MB | Composables, bitmaps |
| Game State | 25MB | Domain models, saves |
| AI/Network | 15MB | Response caching, buffers |
| Graphics | 25MB | Bitmaps, textures |

## Common Leak Sources to Monitor

1. **Static References**: Avoid static Context, View references
2. **Unclosed Resources**: Cursor, InputStream, Bitmap.recycle()
3. **Listener Registration**: Always unregister in onDestroy/onStop
4. **Coroutines**: Use lifecycleScope, viewModelScope
5. **Compose State**: Avoid capturing large objects in lambdas

## Leak Detection

```kotlin
// Add LeakCanary for debug builds
dependencies {
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
```

## Optimization Strategies

1. **Bitmap Handling**: Use Coil/Glide with memory caching
2. **Lazy Loading**: Load game assets on-demand
3. **Object Pooling**: Reuse frequently created objects
4. **String Interning**: Be careful with string concatenation
5. **Collection Sizing**: Pre-size ArrayLists, HashMaps
