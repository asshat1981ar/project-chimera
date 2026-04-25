# Startup Performance Baseline

## Target Metrics

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| Cold Start (Time to First Frame) | <500ms | Startup DEX Trace / Trace events |
| Application.onCreate to First Frame | <400ms | Manual tracing |
| Activity.onCreate to First Draw | <200ms | Activity lifecycle + ReportFullyDrawn |

## Measurement Setup

### Startup Tracing Implementation

The `ChimeraApplication` class implements startup tracing using Android's `Trace` API:

```kotlin
@HiltAndroidApp
class ChimeraApplication : Application(), Configuration.Provider {
    val startupTimestamp: Long = System.currentTimeMillis()
    private var startupTraceSection: String? = null

    override fun onCreate() {
        super.onCreate()
        Trace.beginSection("ChimeraApplication.onCreate")
        startupTraceSection = "Application.onCreate"
        // ... initialization code
    }

    fun onFirstFrameDrawn() {
        startupTraceSection?.let { Trace.endSection() }
        startupTraceSection = null
    }
}
```

### Profiling Commands

```bash
# Cold start trace (method-level)
adb shell am profile start --streaming com.chimera.ashes /sdcard/startup.trace
adb shell am start -n com.chimera.ashes/.ui.MainActivity
adb shell am profile stop

# View in Android Studio: File > Open > startup.trace

# DEX method tracing (more detailed)
adb shell am set-debug-app --persistent com.chimera.ashes
adb shell am set-instrument-app com.chimera.ashes androidx.benchmark.integration.macrobenchmark.target.benchmark.startup.StartupBenchmark

# Quick cold start measurement
adb shell am start -W -n com.chimera.ashes/.ui.MainActivity
# Look for "WaitTime" and "TotalTime" in output
```

## Baseline Measurements

### Device: [Device Name]
### Date: [Measurement Date]

| Test Run | Cold Start (ms) | Warm Start (ms) | Hot Start (ms) |
|----------|-----------------|-----------------|----------------|
| Run 1    | -               | -               | -              |
| Run 2    | -               | -               | -              |
| Run 3    | -               | -               | -              |
| Average  | -               | -               | -              |

## Optimization Opportunities

1. **Lazy Initialization**: Defer non-critical work from Application.onCreate
2. **Hilt Startup**: Use Hilt's lazy injection where possible
3. **WorkManager**: Move background tasks to WorkManager with constraints
4. **Content Providers**: Use Startup library for ordered initialization
5. **Class Loading**: Pre-load critical classes during idle time

## Monitoring

- Add startup time to crash reports
- Track in production via Firebase Performance Monitoring
- Set up alerts for regressions >10%
