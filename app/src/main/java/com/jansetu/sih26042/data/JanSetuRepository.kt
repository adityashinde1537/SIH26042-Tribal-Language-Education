package com.jansetu.sih26042.data

import com.jansetu.sih26042.data.local.TranslationDao
import com.jansetu.sih26042.data.local.TranslationEntity
import com.jansetu.sih26042.data.remote.ApiFactory
import com.jansetu.sih26042.data.remote.Flashcard
import com.jansetu.sih26042.data.remote.FlashcardRequest
import com.jansetu.sih26042.data.remote.TranslationRequest
import com.jansetu.sih26042.data.remote.WorksheetRequest
import com.jansetu.sih26042.data.remote.WorksheetResponse
import com.jansetu.sih26042.data.remote.WorksheetRow
import java.io.IOException
import org.json.JSONObject
import retrofit2.HttpException

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
    private val dao: TranslationDao
) {
    private fun normalize(text: String): String = text.trim().replace(Regex("\\s+"), " ")

    private val builtInDemo = mapOf(
        "नमस्कार विद्यार्थी" to "ᱦᱚᱞᱮᱹᱥ ᱥᱮᱪᱮᱫᱤᱭᱟᱹ ᱾",
        "मेरा कॉलेज है...." to "ᱤᱧᱟᱹᱜ ᱠᱚᱞᱮᱡᱽ ᱢᱮᱱᱟᱜᱼᱟ ᱾",
        "हमारा राज्य झारखंड है।" to "ᱤᱧᱟᱹᱜ ᱯᱚᱱᱚᱛ ᱫᱚ ᱦᱩᱭᱩᱜ ᱠᱟᱱᱟ ᱡᱷᱟᱨᱠᱷᱚᱸᱰ ᱾",
        "सॉफ्टवेयर क्या है?" to "ᱥᱳᱯᱴᱳᱭᱟᱨ ᱫᱚ ᱪᱮᱫ?",
        "नमस्ते, आप कैसे हैं?" to "ᱦᱚᱞᱮ, ᱟᱢ ᱪᱮᱫ ᱞᱮᱠᱟ?",
        "आज मौसम अच्छा है।" to "ᱛᱮᱦᱮᱧ ᱦᱚᱭᱦᱩᱫᱤᱥ ᱱᱟᱯᱟᱭ ᱠᱟᱱᱟ ᱾"
    )

    fun backendUrl(): String = ApiFactory.backendUrl()

    fun saveBackendUrl(url: String): Result<String> = runCatching {
        ApiFactory.setBackendUrl(url)
    }

    suspend fun testBackend(): Result<String> = runCatching {
        val health = ApiFactory.api().health()
        require(health.status == "ok") { "Backend health response was not OK" }
        "Connected • mode=" + health.translationMode +
            " • modelLoaded=" + health.modelLoaded +
            " • sourceTerms=" + health.sourceLexiconItems +
            " • " + ApiFactory.backendUrl()
    }

    private fun backendDetail(http: HttpException): String? {
        return try {
            val raw = http.response()?.errorBody()?.string().orEmpty()
            if (raw.isBlank()) null else JSONObject(raw).optString("detail").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun httpProblem(http: HttpException, endpoint: String): IOException {
        val current = ApiFactory.backendUrl()
        val detail = backendDetail(http)
        return when {
            detail != null -> IOException(
                "Backend error " + http.code() + " at " + endpoint + ": " + detail,
                http
            )
            http.code() == 404 -> IOException(
                "Backend is reachable, but " + endpoint + " returned HTTP 404. " +
                    "Save the API root URL, not the Swagger /docs URL. Current base: " + current,
                http
            )
            else -> IOException(
                "Backend returned HTTP " + http.code() + " for " + endpoint + ". Base: " + current,
                http
            )
        }
    }

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

        val builtIn = builtInDemo[source]
        if (builtIn != null) {
            dao.upsert(TranslationEntity(source, builtIn, "built-in-demo"))
            return@runCatching TranslationResult(
                translatedText = builtIn,
                latencyMs = 0,
                source = "built-in-demo",
                offline = true
            )
        }

        try {
            val response = ApiFactory.api().translate(TranslationRequest(source))
            dao.upsert(TranslationEntity(source, response.translatedText, response.engine))
            TranslationResult(response.translatedText, response.latencyMs, response.engine, false)
        } catch (http: HttpException) {
            throw httpProblem(http, "/translate")
        } catch (network: IOException) {
            composeOffline(source)
                ?: throw IOException(
                    "No offline translation is available for this sentence. " +
                        "Check the backend connection or use a bundled demo phrase.",
                    network
                )
        }
    }

    suspend fun syncSeed(): Result<Int> = runCatching {
        val builtIns = builtInDemo.map { (source, target) ->
            TranslationEntity(normalize(source), target, "built-in-demo")
        }
        dao.upsertAll(builtIns)

        val remoteItems = try {
            ApiFactory.api().seedPack().items
        } catch (_: Exception) {
            emptyList()
        }

        if (remoteItems.isNotEmpty()) {
            dao.upsertAll(
                remoteItems.map {
                    TranslationEntity(normalize(it.hindi), it.santhali, "seed-pack")
                }
            )
        }

        (builtInDemo.keys + remoteItems.map { normalize(it.hindi) }).toSet().size
    }

    suspend fun syncFullLexicon(pageSize: Int = 20): Result<LexiconSyncResult> = runCatching {
        val api = ApiFactory.api()
        val initial = try {
            api.lexiconMeta()
        } catch (http: HttpException) {
            throw httpProblem(http, "/lexicon/meta")
        }

        var offset = 0
        var downloaded = 0

        while (offset < initial.sourceTerms) {
            val page = try {
                api.lexiconPage(offset = offset, limit = pageSize, generate = true)
            } catch (http: HttpException) {
                throw httpProblem(http, "/lexicon/page")
            }

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
            ApiFactory.api().worksheet(WorksheetRequest(title, prompts, nipunDomain))
        } catch (http: HttpException) {
            throw httpProblem(http, "/materials/worksheet")
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
            ApiFactory.api().flashcards(FlashcardRequest(terms, nipunDomain)).cards
        } catch (http: HttpException) {
            throw httpProblem(http, "/materials/flashcards")
        } catch (network: IOException) {
            terms.map { text ->
                val local = translate(text).getOrThrow()
                Flashcard(text.trim(), local.translatedText, "🔤", nipunDomain)
            }
        }
    }
}
