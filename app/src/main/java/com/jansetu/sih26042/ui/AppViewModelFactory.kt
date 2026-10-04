package com.jansetu.sih26042.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jansetu.sih26042.JanSetuApplication
import com.jansetu.sih26042.data.JanSetuRepository

class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    private val repository: JanSetuRepository
        get() = (application as JanSetuApplication).repository

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(TranslationViewModel::class.java) -> TranslationViewModel(repository) as T
        modelClass.isAssignableFrom(VoiceViewModel::class.java) -> VoiceViewModel(repository) as T
        modelClass.isAssignableFrom(MaterialsViewModel::class.java) -> MaterialsViewModel(repository) as T
        modelClass.isAssignableFrom(OfflineViewModel::class.java) -> OfflineViewModel(repository) as T
        else -> error("Unknown ViewModel: ${modelClass.name}")
    }
}
