package com.jansetu.sih26042.data.remote

import com.jansetu.sih26042.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: JanSetuApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.JANSETU_API_BASE_URL.ensureSlash())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JanSetuApi::class.java)
    }

    private fun String.ensureSlash(): String = if (endsWith("/")) this else "$this/"
}
