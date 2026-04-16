package com.chimera

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point.
 * Implements [Configuration.Provider] so WorkManager uses [HiltWorkerFactory],
 * enabling constructor injection in [com.chimera.workers.NpcPortraitSyncWorker]
 * and any future workers.
 *
 * IMPORTANT: Do NOT call WorkManager.initialize() anywhere else — the on-demand
 * initializer picks up Configuration.Provider automatically.
 */
@HiltAndroidApp
class ChimeraApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
