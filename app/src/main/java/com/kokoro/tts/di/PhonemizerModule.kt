package com.kokoro.tts.di

import com.kokoro.tts.Phonemizer
import com.kokoro.tts.PhonemizerApi
import com.kokoro.tts.data.repository.PhonemizerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class, ServiceComponent::class, SingletonComponent::class)
object PhonemizerModule {

    @Provides
    fun providePhonemizerRepository(api: PhonemizerApi): PhonemizerRepository {
        return PhonemizerRepository(api)
    }

    @Provides
    fun providePhonemizer(repository: PhonemizerRepository): Phonemizer {
        return Phonemizer(repository)
    }
}