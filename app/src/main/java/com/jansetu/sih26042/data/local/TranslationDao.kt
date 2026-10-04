package com.jansetu.sih26042.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translations WHERE sourceText = :source LIMIT 1")
    suspend fun find(source: String): TranslationEntity?

    @Query("SELECT * FROM translations WHERE sourceText IN (:sources)")
    suspend fun findMany(sources: List<String>): List<TranslationEntity>

    @Query(
        """
        SELECT * FROM translations
        WHERE (:query = '' OR sourceText LIKE '%' || :query || '%')
        ORDER BY sourceText COLLATE NOCASE
        LIMIT :limit
        """
    )
    suspend fun browse(query: String, limit: Int = 100): List<TranslationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TranslationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TranslationEntity>)

    @Query("SELECT COUNT(*) FROM translations")
    suspend fun count(): Int
}
