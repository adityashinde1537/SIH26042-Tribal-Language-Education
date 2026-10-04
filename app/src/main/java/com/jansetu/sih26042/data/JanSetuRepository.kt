package com.jansetu.sih26042.data

import com.jansetu.sih26042.data.local.TranslationDao
import com.jansetu.sih26042.data.local.TranslationEntity
import com.jansetu.sih26042.data.remote.Flashcard
import com.jansetu.sih26042.data.remote.FlashcardRequest
import com.jansetu.sih26042.data.remote.JanSetuApi
import com.jansetu.sih26042.data.remote.TranslationRequest
import com.jansetu.sih26042.data.remote.WorksheetRequest
import com.jansetu.sih26042.data.remote.WorksheetResponse
import com.jansetu.sih26042.data.remote.WorksheetRow
import java.io.IOException

data class TranslationResult(
    val translatedText: String,
    val latencyMs: Long?,
    val source: String,
    val offline: Boolean
)

data class LexiconSyncResult(
    val downloaded: Int,
    val serverTotal: Int,
    val sourceTerms: Int,
    val completeOnServer: Boolean
)

class JanSetuRepository(
    private val api: JanSetuApi,
    private val dao: TranslationDao
) {
    private fun normalize(text: String): String = text.trim().replace(Regex("\\s+"), " ")

    private suspend fun composeOffline(source: String): TranslationResult? {
        val rawTokens = source.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (rawTokens.size < 2) return null

        val punctuation = charArrayOf('।', '.', '?', '!', ',', ';', ':')
        val cleanTokens = rawTokens.map { token -> token.trim(*punctuation) }
            .filter { it.isNotBlank() }
        if (cleanTokens.size != rawTokens.size) return null

        val entries = dao.findMany(cleanTokens).associateBy { normalize(it.sourceText) }
        if (cleanTokens.any { entries[it] == null }) return null

        val translated = cleanTokens.joinToString(" ") { token ->
            entries.getValue(token).translatedText
                .trim()
                .trimEnd('᱾', '.', '?', '!', ',', ';', ':')
        } + when {
            source.trimEnd().endsWith("?") -> "?"
            source.trimEnd().endsWith("!") -> "!"
            else -> " ᱾"
        }

        return TranslationResult(
            translatedText = translated,
            latencyMs = 0,
            source = "offline-word-composition",
            offline = true
        )
    }

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
            composeOffline(source)
                ?: throw IOException(
                    "This sentence is not fully available offline. Sync its words once or connect to the JanSetu backend.",
                    network
                )
        }
    }

    suspend fun syncSeed(): Result<Int> = runCatching {
        val pack = api.seedPack()
        dao.upsertAll(pack.items.map { TranslationEntity(normalize(it.hindi), it.santhali, "seed-pack") })
        pack.items.size
    }

    suspend fun syncFullLexicon(pageSize: Int = 20): Result<LexiconSyncResult> = runCatching {
        val initial = api.lexiconMeta()
        var offset = 0
        var downloaded = 0

        while (offset < initial.sourceTerms) {
            val page = api.lexiconPage(offset = offset, limit = pageSize, generate = true)
            if (page.items.isNotEmpty()) {
                dao.upsertAll(
                    page.items.map {
                        TranslationEntity(
                            sourceText = normalize(it.hindi),
                            translatedText = it.santhali,
                            engine = "full-lexicon"
                        )
                    }
                )
                downloaded += page.items.size
            }
            offset += page.limit
        }

        val finalMeta = api.lexiconMeta()
        LexiconSyncResult(
            downloaded = downloaded,
            serverTotal = finalMeta.translatedEntries,
            sourceTerms = finalMeta.sourceTerms,
            completeOnServer = finalMeta.complete
        )
    }

    suspend fun offlineCount(): Int = dao.count()

    suspend fun worksheet(
        title: String,
        prompts: List<String>,
        nipunDomain: String = "Vocabulary"
    ): Result<WorksheetResponse> = runCatching {
        try {
            api.worksheet(WorksheetRequest(title, prompts, nipunDomain))
        } catch (network: IOException) {
            val rows = prompts.mapIndexed { index, text ->
                val local = translate(text).getOrThrow()
                WorksheetRow(index + 1, text.trim(), local.translatedText, nipunDomain)
            }
            WorksheetResponse(title, nipunDomain, rows, "")
        }
    }

    suspend fun flashcards(
        terms: List<String>,
        nipunDomain: String = "Vocabulary"
    ): Result<List<Flashcard>> = runCatching {
        try {
            api.flashcards(FlashcardRequest(terms, nipunDomain)).cards
        } catch (network: IOException) {
            terms.map { text ->
                val local = translate(text).getOrThrow()
                Flashcard(text.trim(), local.translatedText, "🔤", nipunDomain)
            }
        }
    }
}
