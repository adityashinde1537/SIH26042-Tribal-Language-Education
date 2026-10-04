package com.jansetu.sih26042.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface JanSetuApi {
    @POST("translate")
    suspend fun translate(@Body request: TranslationRequest): TranslationResponse

    @GET("sync/seed")
    suspend fun seedPack(): SeedPackResponse

    @POST("materials/worksheet")
    suspend fun worksheet(@Body request: WorksheetRequest): WorksheetResponse

    @POST("materials/flashcards")
    suspend fun flashcards(@Body request: FlashcardRequest): FlashcardResponse
}
