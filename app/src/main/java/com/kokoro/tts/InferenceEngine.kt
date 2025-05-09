package com.kokoro.tts

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import android.content.Context
import android.content.res.AssetManager
import android.speech.tts.SynthesisCallback
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import java.nio.ByteBuffer



@Singleton
class InferenceEngine @Inject constructor(
    private val contextManager: Context,
    private val assetManager: AssetManager,
    private val phonemizer: Phonemizer,
    private val coroutineScope: CoroutineScope
) {
    private val ortEnv = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val styleVectors: Map<String, FloatArray>

    init {
        // Load ONNX model
        // Copy model to internal storage
        val infile = assetManager.open("kokoro.onnx")
        val outfile = File(contextManager.filesDir, "kokoro.onnx")
        outfile.outputStream().use { out ->
            infile.copyTo(out)
        }
// Now load from a real file path
        session = ortEnv.createSession(outfile.absolutePath)


        // Explicitly specify type parameters
        styleVectors = listOf<String>("af_heart", "af_bella", "af_nicole").associateWith { voice ->
            assetManager.open("$voice.vec").use { stream ->
                stream.bufferedReader().readLines()
                    .flatMap { line -> line.split(" ").map { it.toFloat() } }
                    .toFloatArray()
            }
        }
    }

    suspend fun synthesize(text: String, voice: String): FloatArray? {
        return try {
            // Add timing logs
            Log.d("Inference", "Starting synthesis for: ${text.take(20)}...")
            val startTime = System.currentTimeMillis()

            val phonemes = phonemizer.phonemize(text) ?: return null
            val inputIds = phonemizer.textToTokenIds(phonemes)

            // Validate input length
            if (inputIds.isEmpty()) {
                Log.w("Inference", "Empty input tokens")
                return null
            }

            val styleVec = styleVectors[voice] ?: styleVectors.values.first()

            val results = session.run(mapOf(
                "input_ids" to OnnxTensor.createTensor(ortEnv, inputIds),
                "style_vector" to OnnxTensor.createTensor(ortEnv, styleVec)
            ))

            val audio = (results[0].value as Array<FloatArray>)[0]
            Log.d("Inference", "Synthesis completed in ${System.currentTimeMillis() - startTime}ms")

            audio
        } catch (e: Exception) {
            Log.e("Inference", "Synthesis failed: ${e.stackTraceToString()}")
            null
        }
    }

}