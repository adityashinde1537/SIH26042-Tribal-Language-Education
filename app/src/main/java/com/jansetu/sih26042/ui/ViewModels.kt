package com.jansetu.sih26042.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jansetu.sih26042.data.JanSetuRepository
import com.jansetu.sih26042.data.LibraryItem
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
                state = state.copy(
                    busy = false,
                    output = it.translatedText,
                    latencyMs = it.latencyMs,
                    source = it.source,
                    offline = it.offline
                )
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
                state = state.copy(
                    busy = false,
                    santhali = it.translatedText,
                    roundTripMs = elapsed,
                    message = null
                )
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
    val domain: String = "Vocabulary",
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
    fun domain(value: String) { state = state.copy(domain = value) }
    private fun lines() = state.content.lines().map { it.trim() }.filter { it.isNotBlank() }

    fun worksheet() = viewModelScope.launch {
        val prompts = lines(); if (prompts.isEmpty()) return@launch
        state = state.copy(busy = true, message = null, cards = emptyList())
        repository.worksheet(state.title, prompts, state.domain).onSuccess {
            state = state.copy(busy = false, worksheet = it)
        }.onFailure {
            state = state.copy(busy = false, message = it.message)
        }
    }

    fun flashcards() = viewModelScope.launch {
        val terms = lines(); if (terms.isEmpty()) return@launch
        state = state.copy(busy = true, message = null, worksheet = null)
        repository.flashcards(terms, state.domain).onSuccess {
            state = state.copy(busy = false, cards = it)
        }.onFailure {
            state = state.copy(busy = false, message = it.message)
        }
    }
}

data class OfflineUiState(
    val count: Int = 0,
    val busy: Boolean = false,
    val message: String? = null,
    val serverTranslated: Int = 0,
    val sourceTerms: Int = 0,
    val backendUrl: String = ""
)

class OfflineViewModel(private val repository: JanSetuRepository) : ViewModel() {
    var state by mutableStateOf(OfflineUiState())
        private set

    fun refresh() = viewModelScope.launch {
        state = state.copy(
            count = repository.offlineCount(),
            backendUrl = repository.backendUrl()
        )
    }

    fun backendUrl(value: String) {
        state = state.copy(backendUrl = value, message = null)
    }

    fun saveBackendUrl() {
        repository.saveBackendUrl(state.backendUrl).onSuccess { saved ->
            state = state.copy(
                backendUrl = saved,
                message = "Backend URL saved as " + saved
            )
        }.onFailure {
            state = state.copy(message = it.message ?: "Invalid backend URL")
        }
    }

    fun testBackend() = viewModelScope.launch {
        state = state.copy(busy = true, message = "Testing backend…")
        repository.testBackend().onSuccess { result ->
            state = state.copy(
                busy = false,
                backendUrl = repository.backendUrl(),
                message = result
            )
        }.onFailure {
            state = state.copy(
                busy = false,
                message = it.message ?: "Backend test failed"
            )
        }
    }

    fun syncStarter() = viewModelScope.launch {
        state = state.copy(busy = true, message = null)
        repository.syncSeed().onSuccess { loaded ->
            state = state.copy(
                busy = false,
                count = repository.offlineCount(),
                message = "Loaded " + loaded + " demo translations. They work without a backend or internet."
            )
        }.onFailure {
            state = state.copy(busy = false, message = it.message ?: "Demo pack load failed")
        }
    }

    fun syncFullLexicon() = viewModelScope.launch {
        state = state.copy(busy = true, message = "Connecting to " + state.backendUrl + "…")
        repository.syncFullLexicon().onSuccess { result ->
            val localCount = repository.offlineCount()
            val status = if (result.completeOnServer) {
                "Full translated lexicon synced: " + result.downloaded + " entries."
            } else {
                "Synced " + result.downloaded + " entries. Server currently has " +
                    result.serverTotal + "/" + result.sourceTerms + " translated."
            }
            state = state.copy(
                busy = false,
                count = localCount,
                serverTranslated = result.serverTotal,
                sourceTerms = result.sourceTerms,
                message = status
            )
        }.onFailure {
            state = state.copy(
                busy = false,
                message = it.message ?: "Full lexicon sync failed"
            )
        }
    }
}


data class LibraryUiState(
    val query: String = "",
    val items: List<LibraryItem> = emptyList(),
    val totalStored: Int = 0,
    val busy: Boolean = false,
    val message: String? = null
)

class LibraryViewModel(private val repository: JanSetuRepository) : ViewModel() {
    var state by mutableStateOf(LibraryUiState())
        private set

    fun query(value: String) {
        state = state.copy(query = value, message = null)
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        state = state.copy(busy = true, message = null)
        val total = repository.offlineCount()
        val results = repository.browseLibrary(state.query, limit = 100)
        state = state.copy(
            busy = false,
            totalStored = total,
            items = results,
            message = when {
                total == 0 -> "No translations are stored yet. Load the demo pack or sync the library first."
                results.isEmpty() && state.query.isNotBlank() -> "Not stored yet. You can translate and save this Hindi term."
                else -> null
            }
        )
    }

    fun translateAndSave() {
        val text = state.query.trim()
        if (text.isBlank() || state.busy) return
        viewModelScope.launch {
            state = state.copy(busy = true, message = "Translating and saving…")
            repository.translate(text).onSuccess {
                val total = repository.offlineCount()
                val results = repository.browseLibrary(text, limit = 100)
                state = state.copy(
                    busy = false,
                    totalStored = total,
                    items = results,
                    message = "Saved to offline translation library."
                )
            }.onFailure {
                state = state.copy(
                    busy = false,
                    message = it.message ?: "Translation could not be saved."
                )
            }
        }
    }

    fun voiceSearch(recognizedHindi: String) {
        val text = recognizedHindi.trim()
        if (text.isBlank() || state.busy) return
        viewModelScope.launch {
            state = state.copy(
                query = text,
                busy = true,
                message = "Recognized Hindi: " + text + " • searching library…"
            )

            val existing = repository.browseLibrary(text, limit = 100)
            if (existing.isNotEmpty()) {
                state = state.copy(
                    busy = false,
                    totalStored = repository.offlineCount(),
                    items = existing,
                    message = "Found in offline translation library."
                )
                return@launch
            }

            repository.translate(text).onSuccess {
                val total = repository.offlineCount()
                val saved = repository.browseLibrary(text, limit = 100)
                state = state.copy(
                    busy = false,
                    totalStored = total,
                    items = saved,
                    message = if (it.offline) {
                        "Found offline and loaded from " + it.source + "."
                    } else {
                        "Voice translation completed and saved for offline use."
                    }
                )
            }.onFailure {
                state = state.copy(
                    busy = false,
                    items = emptyList(),
                    message = it.message ?: "Voice translation failed."
                )
            }
        }
    }

    fun voiceListening() {
        state = state.copy(busy = true, message = "Listening for Hindi speech…")
    }

    fun voiceFailure(message: String) {
        state = state.copy(busy = false, message = message)
    }
}
