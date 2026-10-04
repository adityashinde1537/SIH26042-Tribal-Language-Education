package com.jansetu.sih26042.data.remote

import com.google.gson.annotations.SerializedName

data class TranslationRequest(val text: String)

data class TranslationResponse(
    @SerializedName("translated_text") val translatedText: String,
    @SerializedName("source_language") val sourceLanguage: String,
    @SerializedName("target_language") val targetLanguage: String,
    @SerializedName("latency_ms") val latencyMs: Long,
    val cached: Boolean,
    val engine: String
)

data class HealthResponse(
    val status: String,
    val model: String,
    @SerializedName("model_loaded") val modelLoaded: Boolean,
    val runtime: String,
    @SerializedName("offline_seed_items") val offlineSeedItems: Int,
    @SerializedName("source_lexicon_items") val sourceLexiconItems: Int,
    @SerializedName("translated_lexicon_items") val translatedLexiconItems: Int
)

data class SeedItem(
    val hindi: String,
    val santhali: String,
    @SerializedName("review_status") val reviewStatus: String
)
data class SeedPackResponse(
    val version: String,
    @SerializedName("language_pair") val languagePair: String,
    val items: List<SeedItem>
)

data class LexiconEntry(val hindi: String, val santhali: String)
data class LexiconMetaResponse(
    @SerializedName("source_terms") val sourceTerms: Int,
    @SerializedName("translated_entries") val translatedEntries: Int,
    val complete: Boolean,
    @SerializedName("page_size_max") val pageSizeMax: Int
)
data class LexiconPageResponse(
    val offset: Int,
    val limit: Int,
    val total: Int,
    val items: List<LexiconEntry>
)

data class WorksheetRequest(
    val title: String,
    val prompts: List<String>,
    @SerializedName("nipun_domain") val nipunDomain: String = "Vocabulary"
)
data class WorksheetRow(
    val number: Int,
    val hindi: String,
    val santhali: String,
    @SerializedName("nipun_domain") val nipunDomain: String
)
data class WorksheetResponse(
    val title: String,
    @SerializedName("nipun_domain") val nipunDomain: String,
    val rows: List<WorksheetRow>,
    @SerializedName("printable_html") val printableHtml: String
)

data class FlashcardRequest(
    val terms: List<String>,
    @SerializedName("nipun_domain") val nipunDomain: String = "Vocabulary"
)
data class Flashcard(
    val hindi: String,
    val santhali: String,
    val visual: String,
    @SerializedName("nipun_domain") val nipunDomain: String
)
data class FlashcardResponse(val cards: List<Flashcard>)
