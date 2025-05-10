// app/src/main/java/com/kokoro/tts/data/repository/TokenizerRepository.kt
package com.kokoro.tts.data.repository

import android.util.Log
import com.kokoro.tts.data.api.TokenizerApi
import com.kokoro.tts.data.db.PhonemeLookupDao
import com.kokoro.tts.data.model.MToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenizerRepository @Inject constructor(
    private val tokenizerApi: TokenizerApi,
    private val phonemeLookupDao: PhonemeLookupDao
) {
    /**
     * Processes text by checking local DB first, then fallback to API
     */
    suspend fun processText(text: String): List<MToken> {
        // Simple tokenization to words (can be improved)
        val words = text.split(Regex("\\s+|(?=[,.!?])|(?<=[,.!?])"))
            .filter { it.isNotBlank() }

        val resultTokens = mutableListOf<MToken>()
        val missingWords = mutableListOf<String>()

        // Check local DB first
        for (word in words) {
            val dbTokens = phonemeLookupDao.getTokensForWord(word)
            if (dbTokens != null) {
                resultTokens.addAll(dbTokens)
                Log.d("TokenizerRepo", "DB hit for: $word")
            } else {
                missingWords.add(word)
                Log.d("TokenizerRepo", "DB miss for: $word")
            }
        }

        // Fetch missing words from API
        if (missingWords.isNotEmpty()) {
            try {
                val missingText = missingWords.joinToString(" ")
                val apiTokens = tokenizerApi.tokenize(missingText).mtokens

                // Group API tokens by word for DB caching
                val currentWord = StringBuilder()
                val currentTokens = mutableListOf<MToken>()

                for (token in apiTokens) {
                    if (token.tag == "WORD" || token.tag == "PUNCT") {
                        if (currentWord.isNotEmpty() && currentTokens.isNotEmpty()) {
                            // Save previous word
                            val word = currentWord.toString().trim()
                            phonemeLookupDao.insertTokens(word, currentTokens.toList())
                            currentWord.clear()
                            currentTokens.clear()
                        }

                        currentWord.append(token.text)
                        currentTokens.add(token)
                    } else {
                        // Continue adding to current word
                        currentTokens.add(token)
                    }
                }

                // Save the last word
                if (currentWord.isNotEmpty() && currentTokens.isNotEmpty()) {
                    phonemeLookupDao.insertTokens(currentWord.toString().trim(), currentTokens)
                }

                // Add all API tokens to result
                resultTokens.addAll(apiTokens)

            } catch (e: Exception) {
                Log.e("TokenizerRepo", "API error for missing words", e)
                // Fall back to dummy tokens if API fails
                for (word in missingWords) {
                    resultTokens.add(
                        MToken(
                            text = word,
                            tag = if (word.matches(Regex("[,.!?]"))) "PUNCT" else "WORD",
                            whitespace = " ",
                            phonemes = "", // Empty phonemes as fallback
                            startTs = null,
                            endTs = null
                        )
                    )
                }
            }
        }

        return resultTokens
    }

    /**
     * Alternative approach: process full text with API and cache results
     */
    suspend fun phonemizeFullText(text: String): List<MToken> {
        return try {
            val response = tokenizerApi.phonemize(text)

            // Cache results by word
            cacheTokensByWord(response.mtokens)

            response.mtokens
        } catch (e: Exception) {
            Log.e("TokenizerRepo", "Phonemize API error", e)
            // Fall back to word-by-word processing
            processText(text)
        }
    }

    private suspend fun cacheTokensByWord(tokens: List<MToken>) {
        withContext(Dispatchers.IO) {
            val currentWord = StringBuilder()
            val currentTokens = mutableListOf<MToken>()

            for (token in tokens) {
                if (token.tag == "WORD" || token.tag == "PUNCT") {
                    if (currentWord.isNotEmpty() && currentTokens.isNotEmpty()) {
                        phonemeLookupDao.insertTokens(currentWord.toString().trim(), currentTokens.toList())
                        currentWord.clear()
                        currentTokens.clear()
                    }

                    currentWord.append(token.text)
                    currentTokens.add(token)
                } else {
                    // Continue with current word
                    currentTokens.add(token)
                }
            }

            // Cache the last word
            if (currentWord.isNotEmpty() && currentTokens.isNotEmpty()) {
                phonemeLookupDao.insertTokens(currentWord.toString().trim(), currentTokens)
            }
        }
    }
}