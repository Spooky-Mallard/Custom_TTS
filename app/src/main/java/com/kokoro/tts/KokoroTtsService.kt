// app/src/main/java/com/kokoro/tts/KokoroTtsService.kt
package com.kokoro.tts

import android.speech.tts.SynthesisCallback
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.speech.tts.SynthesisRequest
import android.speech.tts.Voice
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class KokoroTtsService : TextToSpeechService() {

    @Inject lateinit var inferenceEngine: InferenceEngine

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private val supportedVoices = listOf(
        Voice(
            "af_heart", // Voice ID
            Locale("eng", "USA"), // Locale (e.g., English)
            Voice.QUALITY_HIGH, // Quality
            Voice.LATENCY_NORMAL, // Latency
            false, // Requires network? (Now false as we have local fallback)
            emptySet() // Extra features
        ),
        Voice(
            "af_bella",
            Locale("eng", "USA"),
            Voice.QUALITY_HIGH,
            Voice.LATENCY_NORMAL,
            false,
            emptySet()
        )
    )

    override fun onCreate() {
        super.onCreate()
        Log.d("TtsService", "Service created!")
    }

    override fun onSynthesizeText(request: SynthesisRequest, callback: SynthesisCallback) {
        val text = request.text
        val voice = request.voiceName ?: "af_heart"
        // Handle synthesis parameters
        val pitch = request.pitch / 100f // Convert from TTS scale
        val rate = request.speechRate / 100f

        serviceScope.launch {
            try {
                val audioData = inferenceEngine.synthesize(text, voice)?.let {
                    applyAudioModifications(it, pitch, rate)
                }

                withContext(Dispatchers.Main) {
                    if (audioData != null) {
                        AudioStreamer.stream(audioData, callback)
                    } else {
                        callback.error(TextToSpeech.ERROR_SYNTHESIS)
                    }
                }
            } catch (e: Exception) {
                Log.e("TtsService", "Synthesis error", e)
                withContext(Dispatchers.Main) {
                    callback.error(TextToSpeech.ERROR_SYNTHESIS)
                }
            }
        }
    }

    private fun applyAudioModifications(
        audio: FloatArray,
        pitch: Float,
        rate: Float
    ): FloatArray {
        // Implement actual pitch/rate modification here
        // This is just a placeholder implementation
        return when {
            pitch != 1.0f -> audio.map { it * pitch }.toFloatArray()
            rate != 1.0f -> adjustPlaybackSpeed(audio, rate)
            else -> audio
        }
    }

    private fun adjustPlaybackSpeed(audio: FloatArray, speed: Float): FloatArray {
        // Simple resampling implementation
        val newLength = (audio.size / speed).toInt()
        return FloatArray(newLength) { i ->
            audio.getOrElse((i * speed).toInt()) { 0f }
        }
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        val locale = Locale(lang, country, variant)
        return if (supportedVoices.any { it.locale == locale }) {
            TextToSpeech.LANG_AVAILABLE
        } else {
            TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        val locale = Locale(lang, country, variant)
        return if (supportedVoices.any { voice -> voice.locale == locale }) {
            TextToSpeech.LANG_AVAILABLE
        } else {
            TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    override fun onGetLanguage(): Array<String> = arrayOf("eng", "USA", "")

    override fun onGetVoices(): MutableList<Voice> {
        return supportedVoices.toMutableList()
    }

    override fun onStop() {
        serviceJob.cancel()
    }
}