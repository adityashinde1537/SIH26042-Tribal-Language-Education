package com.jansetu.sih26042

import android.app.Application
import com.jansetu.sih26042.data.JanSetuRepository
import com.jansetu.sih26042.data.local.JanSetuDatabase
import com.jansetu.sih26042.data.remote.ApiFactory

class JanSetuApplication : Application() {
    lateinit var repository: JanSetuRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = JanSetuDatabase.create(this)
        repository = JanSetuRepository(ApiFactory.api, database.translationDao())
    }
}
