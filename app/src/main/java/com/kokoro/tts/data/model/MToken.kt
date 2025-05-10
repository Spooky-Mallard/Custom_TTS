// app/src/main/java/com/kokoro/tts/data/model/MToken.kt
package com.kokoro.tts.data.model

import com.google.gson.annotations.SerializedName

data class MToken(
    val text: String,
    val tag: String,
    val whitespace: String,
    val phonemes: String,
    @SerializedName("start_ts") val startTs: Double? = null,
    @SerializedName("end_ts") val endTs: Double? = null,
    val underscore: MetadataFields? = null
)

data class MetadataFields(
    @SerializedName("alt_phonemes") val altPhonemes: String? = null
)