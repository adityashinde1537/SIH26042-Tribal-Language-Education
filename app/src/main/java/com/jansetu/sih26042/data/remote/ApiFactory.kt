package com.jansetu.sih26042.data.remote

import android.content.Context
import com.jansetu.sih26042.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {
    private const val PREFS = "jansetu_network"
    private const val KEY_BACKEND_URL = "backend_url"

    private lateinit var appContext: Context

    @Volatile private var activeUrl: String? = null
    @Volatile private var activeApi: JanSetuApi? = null

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun backendUrl(): String {
        check(::appContext.isInitialized) { "ApiFactory.init(context) must be called first" }
        return appContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_BACKEND_URL, BuildConfig.JANSETU_API_BASE_URL)
            ?.ensureSlash()
            ?: BuildConfig.JANSETU_API_BASE_URL.ensureSlash()
    }

    fun setBackendUrl(raw: String): String {
        check(::appContext.isInitialized) { "ApiFactory.init(context) must be called first" }
        val normalized = raw.trim().ensureSlash()
        require(normalized.startsWith("http://") || normalized.startsWith("https://")) {
            "Backend URL must start with http:// or https://"
        }

        // Validate URL immediately so a typo is reported before a sync attempt.
        Retrofit.Builder().baseUrl(normalized)

        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BACKEND_URL, normalized)
            .apply()

        synchronized(this) {
            activeUrl = null
            activeApi = null
        }
        return normalized
    }

    fun api(): JanSetuApi {
        val url = backendUrl()
        activeApi?.let { cached ->
            if (activeUrl == url) return cached
        }

        synchronized(this) {
            activeApi?.let { cached ->
                if (activeUrl == url) return cached
            }
            val created = Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JanSetuApi::class.java)
            activeUrl = url
            activeApi = created
            return created
        }
    }

    private fun String.ensureSlash(): String = if (endsWith("/")) this else "$this/"
}
