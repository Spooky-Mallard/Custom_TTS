package com.kokoro.tts.data.model

data class PhonemizeRequest(val text: String)
data class PhonemizeResponse(val phonemes: List<String>)