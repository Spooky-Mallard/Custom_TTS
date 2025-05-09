package com.kokoro.tts

import com.kokoro.tts.data.model.PhonemizeRequest
import com.kokoro.tts.data.model.PhonemizeResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PhonemizerApi {
    @POST("phonemize")
    suspend fun phonemize(@Body request: PhonemizeRequest): PhonemizeResponse
}


