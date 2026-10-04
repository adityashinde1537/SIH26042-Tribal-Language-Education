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

data class SeedItem(val hindi: String, val santhali: String, @SerializedName("review_status") val reviewStatus: String)
data class SeedPackResponse(val version: String, @SerializedName("language_pair") val languagePair: String, val items: List<SeedItem>)

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

data class WorksheetRequest(val title: String, val prompts: List<String>)
data class WorksheetRow(val number: Int, val hindi: String, val santhali: String)
data class WorksheetResponse(val title: String, val rows: List<WorksheetRow>, @SerializedName("printable_html") val printableHtml: String)

data class FlashcardRequest(val terms: List<String>)
data class Flashcard(val hindi: String, val santhali: String)
data class FlashcardResponse(val cards: List<Flashcard>)
