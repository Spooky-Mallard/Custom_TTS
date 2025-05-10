// app/src/main/java/com/kokoro/tts/di/NetworkModule.kt
package com.kokoro.tts.di

import com.kokoro.tts.HardcodedAuthInterceptor
import com.kokoro.tts.PhonemizerApi
import com.kokoro.tts.data.api.TokenizerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(): Interceptor = HardcodedAuthInterceptor()

    @Provides
    @Singleton
    fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://spookymallard-phonemizer.hf.space/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePhonemizerApi(retrofit: Retrofit): PhonemizerApi {
        return retrofit.create(PhonemizerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenizerApi(retrofit: Retrofit): TokenizerApi {
        return retrofit.create(TokenizerApi::class.java)
    }
}