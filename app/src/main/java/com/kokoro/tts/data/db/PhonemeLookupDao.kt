// app/src/main/java/com/kokoro/tts/data/db/PhonemeLookupDao.kt
package com.kokoro.tts.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kokoro.tts.data.model.MToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhonemeDatabase @Inject constructor(
    @ApplicationContext context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "phoneme_lookup.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "phoneme_lookup"
        const val COLUMN_WORD = "word"
        const val COLUMN_MTOKENS = "mtokens"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_WORD TEXT PRIMARY KEY,
                $COLUMN_MTOKENS TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Add migration logic if needed in the future
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}

@Singleton
class PhonemeLookupDao @Inject constructor(
    private val database: PhonemeDatabase,
    private val gson: Gson
) {
    private val type = object : TypeToken<List<MToken>>() {}.type

    suspend fun getTokensForWord(word: String): List<MToken>? = withContext(Dispatchers.IO) {
        try {
            val db = database.readableDatabase
            val cursor = db.query(
                PhonemeDatabase.TABLE_NAME,
                arrayOf(PhonemeDatabase.COLUMN_MTOKENS),
                "${PhonemeDatabase.COLUMN_WORD} = ?",
                arrayOf(word.lowercase().trim()),
                null, null, null
            )

            var result: List<MToken>? = null
            if (cursor.moveToFirst()) {
                val tokensJson = cursor.getString(0)
                result = gson.fromJson(tokensJson, type)
            }
            cursor.close()
            return@withContext result
        } catch (e: Exception) {
            Log.e("PhonemeLookupDao", "Error looking up word: $word", e)
            return@withContext null
        }
    }

    suspend fun insertTokens(word: String, tokens: List<MToken>): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = database.writableDatabase
            val values = ContentValues().apply {
                put(PhonemeDatabase.COLUMN_WORD, word.lowercase().trim())
                put(PhonemeDatabase.COLUMN_MTOKENS, gson.toJson(tokens))
            }

            db.insertWithOnConflict(
                PhonemeDatabase.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            return@withContext true
        } catch (e: Exception) {
            Log.e("PhonemeLookupDao", "Error inserting tokens for word: $word", e)
            return@withContext false
        }
    }

    suspend fun getAllWords(): List<String> = withContext(Dispatchers.IO) {
        val wordList = mutableListOf<String>()
        val db = database.readableDatabase
        val cursor = db.query(
            PhonemeDatabase.TABLE_NAME,
            arrayOf(PhonemeDatabase.COLUMN_WORD),
            null, null, null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                wordList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return@withContext wordList
    }
}