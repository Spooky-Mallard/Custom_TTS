// AudioStreamer.kt
package com.kokoro.tts

import android.media.AudioFormat
import android.speech.tts.SynthesisCallback
import java.nio.ByteBuffer

object AudioStreamer {
    fun stream(pcm: FloatArray, callback: SynthesisCallback) {
        try {
            val sampleRate = 24000 // Match your model's output rate
            val bufferSize = pcm.size * 2 // 16-bit samples

            // Convert float[-1,1] to 16-bit signed PCM
            val byteBuffer = ByteBuffer.allocate(bufferSize)
            for (sample in pcm) {
                val scaled = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt()
                byteBuffer.putShort(scaled.toShort())
            }

            // Configure audio format
            callback.start(sampleRate, AudioFormat.ENCODING_PCM_16BIT, 1)

            // Split into chunks to avoid buffer overflow
            val chunkSize = 4096
            val audioData = byteBuffer.array()
            var offset = 0

            while (offset < audioData.size) {
                val bytesToWrite = minOf(chunkSize, audioData.size - offset)
                val written = callback.audioAvailable(audioData, offset, bytesToWrite)
                if (written < 0) break
                offset += written
            }

            callback.done()
        } catch (e: Exception) {
            callback.error()
        }
    }
}