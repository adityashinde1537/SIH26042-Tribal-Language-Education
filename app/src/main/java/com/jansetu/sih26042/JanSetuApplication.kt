package com.jansetu.sih26042

import android.app.Application
import com.jansetu.sih26042.data.JanSetuRepository
import com.jansetu.sih26042.data.offline.OfflineModelPack
import com.jansetu.sih26042.data.offline.OnDeviceTranslator
import com.jansetu.sih26042.data.local.BundledLibraryImporter
import com.jansetu.sih26042.data.local.JanSetuDatabase
import com.jansetu.sih26042.data.remote.ApiFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JanSetuApplication : Application() {
    lateinit var repository: JanSetuRepository
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ApiFactory.init(this)

        val database = JanSetuDatabase.create(this)
        val dao = database.translationDao()
        repository = JanSetuRepository(dao, OnDeviceTranslator(OfflineModelPack(this)))

        // The presentation APK can include the complete pre-translated lexicon.
        // Importing is idempotent because sourceText is the Room primary key.
        applicationScope.launch {
            BundledLibraryImporter.importIfPresent(this@JanSetuApplication, dao)
        }
    }
}
