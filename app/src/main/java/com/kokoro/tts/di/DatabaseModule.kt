// app/src/main/java/com/kokoro/tts/di/DatabaseModule.kt
package com.kokoro.tts.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kokoro.tts.data.db.PhonemeDatabase
import com.kokoro.tts.data.db.PhonemeLookupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun providePhonemeDatabase(@ApplicationContext context: Context): PhonemeDatabase {
        return PhonemeDatabase(context)
    }

    @Provides
    @Singleton
    fun providePhonemeLookupDao(database: PhonemeDatabase, gson: Gson): PhonemeLookupDao {
        return PhonemeLookupDao(database, gson)
    }
}