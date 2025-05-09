package com.kokoro.tts.di

import android.content.Context
import android.content.res.AssetManager
import com.kokoro.tts.InferenceEngine
import com.kokoro.tts.Phonemizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAssetManager(@ApplicationContext context: Context): AssetManager {
        return context.assets
    }

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideInferenceEngine(
        @ApplicationContext contextManager: Context,
        assetManager: AssetManager,
        phonemizer: Phonemizer,
        coroutineScope: CoroutineScope
    ): InferenceEngine {
        return InferenceEngine(contextManager, assetManager, phonemizer, coroutineScope)
    }
}