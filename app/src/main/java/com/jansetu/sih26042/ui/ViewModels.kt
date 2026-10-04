package com.jansetu.sih26042.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jansetu.sih26042.data.JanSetuRepository
import com.jansetu.sih26042.data.remote.Flashcard
import com.jansetu.sih26042.data.remote.WorksheetResponse
import kotlinx.coroutines.launch

data class TranslationUiState(
    val input: String = "",
    val output: String = "",
    val busy: Boolean = false,
    val message: String? = null,
    val latencyMs: Long? = null,
    val source: String = "",
    val offline: Boolean = false
)

class TranslationViewModel(private val repository: JanSetuRepository) : ViewModel() {
    var state by mutableStateOf(TranslationUiState())
        private set

    fun onInput(value: String) { state = state.copy(input = value, output = "", message = null) }

    fun translate() {
        val text = state.input.trim()
        if (text.isBlank() || state.busy) return
        viewModelScope.launch {
            state = state.copy(busy = true, message = null)
            repository.translate(text).onSuccess {
                state = state.copy(busy = false, output = it.translatedText, latencyMs = it.latencyMs, source = it.source, offline = it.offline)
            }.onFailure {
                state = state.copy(busy = false, message = it.message ?: "Translation failed")
            }
        }
    }
}

data class VoiceUiState(
    val recognizedHindi: String = "",
    val santhali: String = "",
    val busy: Boolean = false,
    val message: String? = null,
    val roundTripMs: Long? = null
)

class VoiceViewModel(private val repository: JanSetuRepository) : ViewModel() {
    var state by mutableStateOf(VoiceUiState())
        private set
    private var cycleStartedAt: Long = 0

    fun beginCycle() {
        cycleStartedAt = System.currentTimeMillis()
        state = VoiceUiState(busy = true, message = "Listening…")
    }

    fun recognized(text: String) {
        state = state.copy(recognizedHindi = text, message = "Translating…")
        viewModelScope.launch {
            repository.translate(text).onSuccess {
                val elapsed = System.currentTimeMillis() - cycleStartedAt
                state = state.copy(busy = false, santhali = it.translatedText, roundTripMs = elapsed, message = null)
            }.onFailure {
                state = state.copy(busy = false, message = it.message ?: "Voice translation failed")
            }
        }
    }

    fun fail(message: String) { state = state.copy(busy = false, message = message) }
}

data class MaterialsUiState(
    val title: String = "FLN Practice Worksheet",
    val content: String = "नमस्ते, आप कैसे हैं?\nआज मौसम अच्छा है।",
    val busy: Boolean = false,
    val worksheet: WorksheetResponse? = null,
    val cards: List<Flashcard> = emptyList(),
    val message: String? = null
)

class MaterialsViewModel(private val repository: JanSetuRepository) : ViewModel() {
    var state by mutableStateOf(MaterialsUiState())
        private set
    fun title(value: String) { state = state.copy(title = value) }
    fun content(value: String) { state = state.copy(content = value) }
    private fun lines() = state.content.lines().map { it.trim() }.filter { it.isNotBlank() }

    fun worksheet() = viewModelScope.launch {
        val prompts = lines(); if (prompts.isEmpty()) return@launch
        state = state.copy(busy = true, message = null, cards = emptyList())
        repository.worksheet(state.title, prompts).onSuccess { state = state.copy(busy = false, worksheet = it) }
            .onFailure { state = state.copy(busy = false, message = it.message) }
    }

    fun flashcards() = viewModelScope.launch {
        val terms = lines(); if (terms.isEmpty()) return@launch
        state = state.copy(busy = true, message = null, worksheet = null)
        repository.flashcards(terms).onSuccess { state = state.copy(busy = false, cards = it) }
            .onFailure { state = state.copy(busy = false, message = it.message) }
    }
}

data class OfflineUiState(val count: Int = 0, val busy: Boolean = false, val message: String? = null)

class OfflineViewModel(private val repository: JanSetuRepository) : ViewModel() {
    var state by mutableStateOf(OfflineUiState())
        private set

    fun refresh() = viewModelScope.launch { state = state.copy(count = repository.offlineCount()) }

    fun sync() = viewModelScope.launch {
        state = state.copy(busy = true, message = null)
        repository.syncSeed().onSuccess {
            state = state.copy(busy = false, count = repository.offlineCount(), message = "Synced $it starter phrases for offline use.")
        }.onFailure {
            state = state.copy(busy = false, message = it.message ?: "Sync failed")
        }
    }
}
