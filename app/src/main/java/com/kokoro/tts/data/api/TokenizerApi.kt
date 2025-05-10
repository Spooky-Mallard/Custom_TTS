// app/src/main/java/com/kokoro/tts/data/api/TokenizerApi.kt
package com.kokoro.tts.data.api

import com.kokoro.tts.data.model.MToken
import retrofit2.http.GET
import retrofit2.http.Query

interface TokenizerApi {
    @GET("tokenize")
    suspend fun tokenize(@Query("text") text: String): TokenizeResponse

    @GET("phonemize")
    suspend fun phonemize(@Query("text") text: String): PhonemeResponse
}

data class TokenizeResponse(val mtokens: List<MToken>)
data class PhonemeResponse(val mtokens: List<MToken>)