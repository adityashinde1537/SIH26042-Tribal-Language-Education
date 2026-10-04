package com.jansetu.sih26042.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface JanSetuApi {
    @GET("health")
    suspend fun health(): HealthResponse

    @POST("translate")
    suspend fun translate(@Body request: TranslationRequest): TranslationResponse

    @GET("sync/seed")
    suspend fun seedPack(): SeedPackResponse

    @GET("lexicon/meta")
    suspend fun lexiconMeta(): LexiconMetaResponse

    @GET("lexicon/page")
    suspend fun lexiconPage(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 20,
        @Query("generate") generate: Boolean = false
    ): LexiconPageResponse

    @POST("materials/worksheet")
    suspend fun worksheet(@Body request: WorksheetRequest): WorksheetResponse

    @POST("materials/flashcards")
    suspend fun flashcards(@Body request: FlashcardRequest): FlashcardResponse
}
