package com.jansetu.sih26042.data

import com.jansetu.sih26042.data.local.TranslationDao
import com.jansetu.sih26042.data.local.TranslationEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineWordTranslationTest {
    private class FakeDao(vararg entries: TranslationEntity): TranslationDao {
        private val data = entries.associateBy { it.sourceText }.toMutableMap()

        override suspend fun find(source: String) = data[source]
        override suspend fun findMany(sources: List<String>) =
            sources.mapNotNull(data::get)
        override suspend fun browse(query: String, limit: Int): List<TranslationEntity> =
            data.values.filter { query in it.sourceText }.take(limit)
        override suspend fun upsert(item: TranslationEntity) { data[item.sourceText] = item }
        override suspend fun upsertAll(items: List<TranslationEntity>) {
            items.forEach { data[it.sourceText] = it }
        }
        override suspend fun count() = data.size
    }

    @Test fun usesLocalDictionaryAndNeverRequiresBackend() = runBlocking {
        val dao = FakeDao(
            TranslationEntity("पानी", "ᱫᱟᱜ", "human-reviewed")
        )
        // No neural model or backend is configured.
        val repository = JanSetuRepository(dao)
        val response = repository.translateWordByWord("पानी और पानी!")
            .getOrThrow()
        assertEquals(3, response.words.size)
        assertEquals(2, response.available)
        assertEquals(listOf("पानी", "और", "पानी"), response.words.map { it.hindi })
        assertEquals("ᱫᱟᱜ", response.words[0].santhali)
        assertEquals(WordTranslationStatus.SAVED, response.words[0].status)
        assertNull(response.words[1].santhali)
        assertEquals(WordTranslationStatus.NOT_AVAILABLE, response.words[1].status)
        assertEquals(WordTranslationStatus.SAVED, response.words[2].status)
    }

    @Test fun unsupportedOrNonOlChikiEntriesRemainMissing() = runBlocking {
        val repository = JanSetuRepository(
            FakeDao(TranslationEntity("किताब", "Book", "invalid-legacy-entry"))
        )
        val result = repository.translateWordByWord("किताब").getOrThrow()
        assertEquals(WordTranslationStatus.NOT_AVAILABLE, result.words.single().status)
    }

    @Test fun rejectsPunctuationWithoutWords() = runBlocking {
        val repository = JanSetuRepository(FakeDao())
        assertTrue(repository.translateWordByWord("।।…!").isFailure)
    }
}
