package com.chimera.di

import com.chimera.network.CloudSaveRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provides the [CloudSaveRepository] singleton.
 *
 * BASE_URL and API_TOKEN are compile-time constants read from BuildConfig.
 * Set them in your local.properties (or CI env):
 *   CHIMERA_CLOUD_SAVE_URL=https://chimera-saves.<your-account>.workers.dev
 *   CHIMERA_CLOUD_SAVE_TOKEN=<your-secret-token>
 *
 * Then in app/build.gradle.kts, inside defaultConfig:
 *   buildConfigField("String","CLOUD_SAVE_URL", "\"${project.findProperty("CHIMERA_CLOUD_SAVE_URL") ?: ""}\"")
 *   buildConfigField("String","CLOUD_SAVE_TOKEN","\"${project.findProperty("CHIMERA_CLOUD_SAVE_TOKEN") ?: ""}\"")
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Named("cloud_save_base_url")
    fun provideCloudSaveBaseUrl(): String =
        try { com.chimera.BuildConfig.CLOUD_SAVE_URL } catch (_: Throwable) { "" }

    @Provides
    @Named("cloud_save_api_token")
    fun provideCloudSaveApiToken(): String =
        try { com.chimera.BuildConfig.CLOUD_SAVE_TOKEN } catch (_: Throwable) { "" }

    @Provides
    @Singleton
    fun provideCloudSaveRepository(
        @Named("cloud_save_base_url")  baseUrl: String,
        @Named("cloud_save_api_token") apiToken: String
    ): CloudSaveRepository = CloudSaveRepository(baseUrl, apiToken)
}
