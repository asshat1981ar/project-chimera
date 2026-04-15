package com.chimera.di

import com.chimera.core.events.GameEventBus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChimeraModule {

    @Provides
    @Singleton
    fun provideGameEventBus(): GameEventBus {
        return GameEventBus()
    }
}
