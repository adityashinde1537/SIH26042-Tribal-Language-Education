package com.jansetu.sih26042.data

import com.jansetu.sih26042.data.local.TranslationDao
import com.jansetu.sih26042.data.local.TranslationEntity
import com.jansetu.sih26042.data.remote.Flashcard
import com.jansetu.sih26042.data.remote.FlashcardRequest
import com.jansetu.sih26042.data.remote.JanSetuApi
import com.jansetu.sih26042.data.remote.TranslationRequest
import com.jansetu.sih26042.data.remote.WorksheetRequest
import com.jansetu.sih26042.data.remote.WorksheetResponse
import java.io.IOException

data class TranslationResult(
    val translatedText: String,
    val latencyMs: Long?,
    val source: String,
    val offline: Boolean
)

class JanSetuRepository(
    private val api: JanSetuApi,
    private val dao: TranslationDao
) {
    private fun normalize(text: String): String = text.trim().replace(Regex("\\s+"), " ")

    suspend fun translate(text: String): Result<TranslationResult> = runCatching {
        val source = normalize(text)
        require(source.isNotBlank()) { "Hindi text cannot be blank" }

        val cached = dao.find(source)
        if (cached != null) {
            return@runCatching TranslationResult(
                translatedText = cached.translatedText,
                latencyMs = 0,
                source = cached.engine,
                offline = true
            )
        }

        try {
            val response = api.translate(TranslationRequest(source))
            dao.upsert(TranslationEntity(source, response.translatedText, response.engine))
            TranslationResult(response.translatedText, response.latencyMs, response.engine, false)
        } catch (network: IOException) {
            throw IOException("This phrase is not in the offline cache. Connect once and sync/translate it first.", network)
        }
    }

    suspend fun syncSeed(): Result<Int> = runCatching {
        val pack = api.seedPack()
        dao.upsertAll(pack.items.map { TranslationEntity(normalize(it.hindi), it.santhali, "seed-pack") })
        pack.items.size
    }

    suspend fun offlineCount(): Int = dao.count()

    suspend fun worksheet(title: String, prompts: List<String>): Result<WorksheetResponse> = runCatching {
        try {
            api.worksheet(WorksheetRequest(title, prompts))
        } catch (network: IOException) {
            val rows = prompts.mapIndexed { index, text ->
                val local = translate(text).getOrThrow()
                com.jansetu.sih26042.data.remote.WorksheetRow(index + 1, text.trim(), local.translatedText)
            }
            WorksheetResponse(title, rows, "")
        }
    }

    suspend fun flashcards(terms: List<String>): Result<List<Flashcard>> = runCatching {
        try {
            api.flashcards(FlashcardRequest(terms)).cards
        } catch (network: IOException) {
            terms.map { text -> Flashcard(text.trim(), translate(text).getOrThrow().translatedText) }
        }
    }
}
