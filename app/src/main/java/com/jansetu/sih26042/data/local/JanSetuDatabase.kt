package com.jansetu.sih26042.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TranslationEntity::class], version = 1, exportSchema = false)
abstract class JanSetuDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao

    companion object {
        fun create(context: Context): JanSetuDatabase = Room.databaseBuilder(
            context.applicationContext,
            JanSetuDatabase::class.java,
            "jansetu-offline.db"
        ).build()
    }
}
