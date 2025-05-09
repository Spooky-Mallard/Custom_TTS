package com.kokoro.tts

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HardcodedAuthInterceptor @Inject constructor() : Interceptor {
    private val token = "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"

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        )
    }
}