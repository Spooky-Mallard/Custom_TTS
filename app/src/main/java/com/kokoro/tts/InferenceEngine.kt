// app/src/main/java/com/kokoro/tts/InferenceEngine.kt
package com.kokoro.tts

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.kokoro.tts.data.model.MToken
import com.kokoro.tts.data.repository.TokenizerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InferenceEngine @Inject constructor(
    private val contextManager: Context,
    private val assetManager: AssetManager,
    private val tokenizerRepository: TokenizerRepository,
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
        styleVectors = listOf("af_heart", "af_bella", "af_nicole").associateWith { voice ->
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

            // Get MTokens using the repository (DB or API)
            val mtokens = tokenizerRepository.processText(text)

            // Convert MTokens to input IDs
            val inputIds = convertTokensToInputIds(mtokens)

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

    private fun convertTokensToInputIds(mtokens: List<MToken>): LongArray {
        // Extract phonemes from MTokens and convert to input IDs
        // This is a simple implementation - you may need to adjust based on your model's requirements
        val phonemesText = mtokens.joinToString(" ") { it.phonemes }

        // Split phonemes into individual tokens
        val phonemeTokens = phonemesText.split(" ")
            .filter { it.isNotBlank() }
            .map { it.trim() }

        // Convert to input IDs (you'll need to implement this based on your tokenizer)
        // For demonstration, we'll use a dummy mapping
        val lookup = mapOf(
            "HH" to 1L, "AH0" to 2L, "L" to 3L, "OW1" to 4L
            // Add all your phoneme mappings here
        )

        val ids = mutableListOf<Long>(0L) // BOS token
        for (p in phonemeTokens) {
            lookup[p]?.let { ids.add(it) }
        }
        ids.add(0L) // EOS token

        return ids.toLongArray()
    }
}