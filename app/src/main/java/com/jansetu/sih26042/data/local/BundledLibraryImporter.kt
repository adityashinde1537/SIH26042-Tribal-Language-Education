package com.jansetu.sih26042.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BundledLibraryImporter {
    private const val ASSET_NAME = "offline_library.tsv"

    suspend fun importIfPresent(
        context: Context,
        dao: TranslationDao
    ): Int = withContext(Dispatchers.IO) {
        val assetNames = context.assets.list("")?.toSet().orEmpty()
        if (ASSET_NAME !in assetNames) return@withContext 0

        val batch = ArrayList<TranslationEntity>(500)
        var imported = 0

        context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach { line ->
                if (line.isBlank()) return@forEach

                val parts = line.split('\t', limit = 2)
                if (parts.size != 2) return@forEach

                val source = parts[0].trim()
                val target = parts[1].trim()
                if (source.isBlank() || target.isBlank()) return@forEach

                batch += TranslationEntity(
                    sourceText = source,
                    translatedText = target,
                    engine = "bundled-full-library"
                )

                if (batch.size >= 500) {
                    dao.upsertAll(batch.toList())
                    imported += batch.size
                    batch.clear()
                }
            }
        }

        if (batch.isNotEmpty()) {
            dao.upsertAll(batch.toList())
            imported += batch.size
        }

        imported
    }
}
