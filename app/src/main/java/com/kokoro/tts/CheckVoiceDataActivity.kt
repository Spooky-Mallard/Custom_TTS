package com.kokoro.tts

import android.speech.tts.TextToSpeech
import android.app.Activity
import android.content.Intent
import android.os.Bundle

class CheckVoiceDataActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Build the result intent with the required extras:
        val result = Intent().apply {
            // List all the resource files you shipped
            putStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES,
                arrayListOf("kokoro_model.onnx")
            )
            // Declare which locales you support
            putStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES,
                arrayListOf("eng-USA")          // or your locale
            )
            // No unavailable voices
            putStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES,
                arrayListOf<String>()
            )
        }
        // Signal PASS and finish
        setResult(TextToSpeech.Engine.CHECK_VOICE_DATA_PASS, result)
        finish()
    }
}
