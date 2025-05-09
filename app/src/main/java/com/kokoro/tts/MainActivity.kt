package com.kokoro.tts

import android.os.Bundle
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var ttsService: KokoroTtsService
    @Inject lateinit var coroutineScope: CoroutineScope

    private lateinit var inputText: EditText
    private lateinit var playButton: Button
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputText = findViewById(R.id.inputText)
        playButton = findViewById(R.id.playButton)
        statusText = findViewById(R.id.statusText)

        playButton.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isNotEmpty()) {
                synthesizeAndPlay(text)
            }
        }
    }


    private fun synthesizeAndPlay(text: String) {
        statusText.text = "Status: Synthesizing..."
        val params = Bundle().apply {
            putString("lang","en")
            putString("country","USA")// 80% speed
        }

        ttsService.onSynthesizeText(
            SynthesisRequest(text,params),
            object : SynthesisCallback{
                override fun getMaxBufferSize(): Int {
                    TODO("Not yet implemented")
                }

                override fun start(sampleRateInHz: Int, audioFormat: Int, channelCount: Int): Int {
                    TODO("Not yet implemented")
                }

                override fun audioAvailable(buffer: ByteArray?, offset: Int, length: Int): Int {
                    TODO("Not yet implemented")
                }

                override fun done(): Int {
                    TODO("Not yet implemented")
                }

                override fun error() {
                    TODO("Not yet implemented")
                }

                override fun error(errorCode: Int) {
                    TODO("Not yet implemented")
                }

                override fun hasStarted(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun hasFinished(): Boolean {
                    TODO("Not yet implemented")
                }
            }
        )
    }
}