// app/src/main/java/com/kokoro/tts/MainActivity.kt
package com.kokoro.tts

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import java.util.UUID

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var playButton: Button
    private lateinit var statusText: TextView

    private var tts: TextToSpeech? = null
    private var ttsInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputText = findViewById(R.id.inputText)
        playButton = findViewById(R.id.playButton)
        statusText = findViewById(R.id.statusText)

        initializeTts()

        playButton.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isNotEmpty() && ttsInitialized) {
                speakText(text)
            } else if (!ttsInitialized) {
                statusText.text = "Status: TTS not initialized yet"
            }
        }
    }

    private fun initializeTts() {
        statusText.text = "Status: Initializing TTS..."
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    statusText.text = "Status: Language not supported"
                    Log.e("TTS", "Language not supported")
                } else {
                    // Set up utterance progress listener
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            runOnUiThread {
                                statusText.text = "Status: Speaking..."
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            runOnUiThread {
                                statusText.text = "Status: Done"
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            runOnUiThread {
                                statusText.text = "Status: Error occurred"
                            }
                        }
                    })

                    // Set your custom TTS engine as default
                    val engines = tts?.engines
                    val kokoroEngine = engines?.find { it.name == "com.kokoro.tts" }
                    if (kokoroEngine != null) {
                        tts?.setEngineByPackageName("com.kokoro.tts")
                        Log.d("TTS", "Using Kokoro TTS engine")
                    } else {
                        Log.w("TTS", "Kokoro TTS engine not found, using system default")
                    }

                    ttsInitialized = true
                    statusText.text = "Status: Ready"
                }
            } else {
                statusText.text = "Status: TTS initialization failed"
                Log.e("TTS", "TTS initialization failed with status: $status")
            }
        }
    }

    private fun speakText(text: String) {
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}