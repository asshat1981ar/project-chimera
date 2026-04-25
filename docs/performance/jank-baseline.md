# Jank Performance Baseline

## Target Metrics

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| Jank Percentage | <5% | FrameMetricsAggregator |
| 90th Percentile Frame Time | <16ms | Perfetto / Systrace |
| 99th Percentile Frame Time | <50ms | Perfetto / Systrace |
| Slow Frames (>16ms) | <5% | FrameMetrics |
| Frozen Frames (>700ms) | 0% | FrameMetrics |

## Measurement Setup

### FrameMetricsAggregator Implementation

Add to `ChimeraApplication.kt` or a dedicated performance module:

```kotlin
class PerformanceTracker @Inject constructor(
    private val application: Application
) {
    private var frameMetricsAggregator: FrameMetricsAggregator? = null
    private var trackingActive = false

    fun startTracking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            frameMetricsAggregator = FrameMetricsAggregator()
            trackingActive = true
        }
    }

    fun stopTracking(): FrameMetricsResult? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !trackingActive) {
            return null
        }

        val aggregator = frameMetricsAggregator ?: return null
        val metrics = aggregator.metrics

        val result = FrameMetricsResult(
            totalFrames = metrics.totalFrames,
            slowFrames = metrics.slowFrames,
            frozenFrames = metrics.frozenFrames,
            jankPercentage = if (metrics.totalFrames > 0) {
                ((metrics.slowFrames + metrics.frozenFrames) / metrics.totalFrames.toFloat()) * 100
            } else 0f,
            averageFrameTimeMs = metrics.averageFrameTimeMs,
            p90FrameTimeMs = metrics.p90FrameTimeMs,
            p99FrameTimeMs = metrics.p99FrameTimeMs
        )

        aggregator.clear()
        frameMetricsAggregator = null
        trackingActive = false
        return result
    }
}

data class FrameMetricsResult(
    val totalFrames: Int,
    val slowFrames: Int,        // >16ms (60fps threshold)
    val frozenFrames: Int,      // >700ms (ANR threshold)
    val jankPercentage: Float,
    val averageFrameTimeMs: Float,
    val p90FrameTimeMs: Float,
    val p99FrameTimeMs: Float
)
```

### Profiling Commands

```bash
# Enable GPU profiling
adb shell dumpsys SurfaceFlinger --latency-dump

# Record frame timing with perfetto
adb shell perfetto -c - --txt -o /data/misc/perfetto/traces/chimera.perfetto

# Surface flinger latency for specific activity
adb shell dumpsys SurfaceFlinger --latency com.chimera.ashes com.chimera.ashes/ui.MainActivity

# Real-time FPS monitoring
adb shell dumpsys Window Windows | grep -E "mCurrentFocus|mFocusedApp"
```

### GPU Rendering Profile

Enable in Developer Options:
- "Profile GPU Rendering" → "On screen as bars"
- Green = good (<16ms)
- Yellow = warning
- Red = jank (>16ms)

## Baseline Measurements

### Device: [Device Name]
### Date: [Measurement Date]

| Screen/Scenario | Avg FPS | Jank % | P90 (ms) | P99 (ms) |
|-----------------|---------|--------|----------|----------|
| Main Menu       | -       | -      | -        | -        |
| Map Screen      | -       | -      | -        | -        |
| Dialogue        | -       | -      | -        | -        |
| Combat          | -       | -      | -        | -        |
| Transitions     | -       | -      | -        | -        |

## Jank Detection in Tests

```kotlin
// Macrobenchmark test for startup jank
@Test
fun startupNoJank() = benchmarkRule.measurePerformanceMetrics("startup") {
    activityRule.launchActivity(null)
    // Assert jank percentage
}

// Compose UI test
@get:Rule
val benchmarkRule = BenchmarkRule()

@Test
fun scrollList_noJank() {
    benchmarkRule.measureRepeated {
        composeTestRule.onNodeWithTag("gameList").performTouchInput {
            swipeUp()
        }
    }
}
```

## Common Jank Causes

1. **Layout Overdraw**: Too many nested layouts, opaque backgrounds
2. **Expensive onDraw**: Complex custom drawing in compose
3. **Main Thread Work**: I/O, JSON parsing, database queries
4. **Bitmap Loading**: Loading large images on main thread
5. **Garbage Collection**: Excessive allocations causing GC pauses
6. **Compose Recomposition**: Unnecessary recompositions

## Optimization Strategies

### Compose-Specific

```kotlin
// Use derivedStateOf for expensive calculations
val scrollState = rememberScrollState()
val showButton = remember { derivedStateOf { scrollState.value > 100 } }

// Stabilize lambdas with remember
val onItemClick = remember { { item: Item -> viewModel.onSelect(item) } }

// Use key() for list items with stable identities
LazyColumn {
    items(items, key = { it.id }) { item ->
        ItemRow(item)
    }
}
```

### General

1. **Profile First**: Use CPU Profiler → Record method traces
2. **Reduce Allocations**: Object pools, reuse arrays
3. **Async Loading**: Coroutines with Dispatchers.Default/IO
4. **Image Optimization**: Coil with memory/disk caching
5. **Layout Flattening**: Merge compose layouts where possible

## Monitoring

- Add FrameMetrics logging to debug builds
- Track jank percentage in analytics
- Set up CI benchmark tests for regressions
