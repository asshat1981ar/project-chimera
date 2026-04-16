package com.chimera.di

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkerFactory
import com.chimera.ai.PortraitGenerationService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    /** Binds HiltWorkerFactory as the WorkerFactory used by ChimeraApplication. */
    @Binds
    abstract fun bindWorkerFactory(factory: HiltWorkerFactory): WorkerFactory

    companion object {

        @Provides
        @Singleton
        fun providePortraitGenerationService(
            @Named("hf_api_token") hfToken: String
        ): PortraitGenerationService = PortraitGenerationService(hfToken)

        @Provides
        @Named("hf_api_token")
        fun provideHfApiToken(): String =
            try { com.chimera.BuildConfig.HUGGING_FACE_TOKEN } catch (_: Throwable) { "" }
    }
}
