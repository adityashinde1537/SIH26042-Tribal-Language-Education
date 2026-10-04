package com.jansetu.sih26042.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey val sourceText: String,
    val translatedText: String,
    val engine: String,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
