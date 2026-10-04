package com.jansetu.sih26042.data.remote

import com.jansetu.sih26042.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {
    val api: JanSetuApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.JANSETU_API_BASE_URL.ensureSlash())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JanSetuApi::class.java)
    }

    private fun String.ensureSlash(): String = if (endsWith("/")) this else "$this/"
}
