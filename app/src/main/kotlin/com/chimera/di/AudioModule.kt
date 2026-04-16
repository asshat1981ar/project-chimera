package com.chimera.di

import android.content.Context
import com.chimera.ai.AudioProvider
import com.chimera.ai.PocketTtsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides [AudioProvider] as a singleton.
 *
 * [PocketTtsProvider] is always constructed (Android TTS init is cheap and lazy).
 * Whether it actually speaks depends on [AppSettings.voiceEnabled], which
 * [DialogueSceneViewModel] observes before calling speak().
 *
 * If TTS initialization fails (engine not installed), PocketTtsProvider silently
 * degrades — no injection change needed.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideAudioProvider(
        @ApplicationContext context: Context
    ): AudioProvider = PocketTtsProvider(context)
}
