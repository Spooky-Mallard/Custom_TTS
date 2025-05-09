package com.kokoro.tts.data.repository

import com.kokoro.tts.PhonemizerApi
import com.kokoro.tts.data.model.PhonemizeRequest
import javax.inject.Inject

class PhonemizerRepository @Inject constructor(
    private val api: PhonemizerApi
) {
    suspend fun phonemize(text: String): List<String>? {
        return try {
            val response = api.phonemize(PhonemizeRequest(text))
            response.phonemes
        } catch (e: Exception) {
            null
        }
    }
}