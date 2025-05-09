package com.kokoro.tts

import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kokoro.tts.data.repository.PhonemizerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Phonemizer @Inject constructor(
    private val repository: PhonemizerRepository
) {
    private var lookup: Map<String, Int> = emptyMap()
    private val initializationLock = Mutex()
    private var isInitialized = false

    suspend fun initialize(assetManager: AssetManager) {
        if (lookup.isEmpty()) {
            val json = withContext(Dispatchers.IO) {
                assetManager.open("g2p_lookup.json").bufferedReader().use { it.readText() }
            }
            lookup = Gson().fromJson<Map<String, Int>>(
                json,
                object : TypeToken<Map<String, Int>>() {}.type
            )
        }
    }

    suspend fun phonemize(text: String): List<String>? {
        return repository.phonemize(text)
    }

    fun textToTokenIds(phonemes: List<String>): LongArray {
        val ids = mutableListOf<Long>(0L)
        for (p in phonemes) {
            lookup[p]?.let { ids.add(it.toLong()) }
        }
        ids.add(0L)
        return ids.toLongArray()
    }
}