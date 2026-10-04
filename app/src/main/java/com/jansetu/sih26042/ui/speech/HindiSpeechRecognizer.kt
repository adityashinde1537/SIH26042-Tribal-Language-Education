package com.jansetu.sih26042.ui.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class HindiSpeechRecognizer(
    private val context: Context,
    private val onListening: () -> Unit,
    private val onResult: (String) -> Unit,
    private val onErrorMessage: (String) -> Unit
) : RecognitionListener {

    private val onDeviceAvailable: Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    private var usingOnDevice: Boolean = false
    private var fallbackAttempted: Boolean = false
    private var recognizer: SpeechRecognizer? = null

    init {
        recognizer = createPreferredRecognizer()
        recognizer?.setRecognitionListener(this)
    }

    val modeLabel: String
        get() = when {
            recognizer == null -> "Speech recognition unavailable"
            usingOnDevice -> "On-device Hindi speech recognition"
            else -> "System Hindi speech recognition"
        }

    val isAvailable: Boolean
        get() = recognizer != null

    private fun createPreferredRecognizer(): SpeechRecognizer? {
        if (onDeviceAvailable) {
            try {
                usingOnDevice = true
                return SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } catch (_: Exception) {
                usingOnDevice = false
            }
        }
        return createSystemRecognizer()
    }

    private fun createSystemRecognizer(): SpeechRecognizer? {
        usingOnDevice = false
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return null
        return try {
            SpeechRecognizer.createSpeechRecognizer(context)
        } catch (_: Exception) {
            null
        }
    }

    private fun recognitionIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")

            // Only force offline when we are actually using Android's on-device
            // recognizer. The system fallback must be allowed to use network
            // recognition if the Hindi offline model is not installed.
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, usingOnDevice)

            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

    fun startListening() {
        fallbackAttempted = false
        startCurrentRecognizer()
    }

    private fun startCurrentRecognizer() {
        val service = recognizer
        if (service == null) {
            onErrorMessage(
                "No Android speech-recognition service is available. " +
                    "Install/enable a speech service or type Hindi manually."
            )
            return
        }

        try {
            service.cancel()
            service.startListening(recognitionIntent())
            onListening()
        } catch (exc: Exception) {
            onErrorMessage(
                "Could not start Hindi speech recognition: " +
                    (exc.message ?: "unknown error")
            )
        }
    }

    private fun switchToSystemAndRetry(): Boolean {
        if (!usingOnDevice || fallbackAttempted) return false

        fallbackAttempted = true
        try {
            recognizer?.cancel()
            recognizer?.destroy()
        } catch (_: Exception) {
        }

        recognizer = createSystemRecognizer()
        recognizer?.setRecognitionListener(this)

        return if (recognizer != null) {
            startCurrentRecognizer()
            true
        } else {
            false
        }
    }

    fun destroy() {
        try {
            recognizer?.cancel()
            recognizer?.destroy()
        } catch (_: Exception) {
        }
        recognizer = null
    }

    private fun errorMessage(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
        SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer client error. Try again."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
        SpeechRecognizer.ERROR_NETWORK -> "Speech recognition needs network access on this device."
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech service network timeout."
        SpeechRecognizer.ERROR_NO_MATCH -> "No Hindi speech was recognized. Please try again."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Wait a moment and try again."
        SpeechRecognizer.ERROR_SERVER -> "Speech recognition service error."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech was detected."
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ->
            "Hindi (India) is not supported by the selected speech recognizer."
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ->
            "Hindi speech data is unavailable. Install Hindi speech data or use network recognition."
        else -> "Speech recognition failed (error " + code + ")."
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit

    override fun onError(error: Int) {
        if (
            usingOnDevice &&
            (error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
                error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE)
        ) {
            if (switchToSystemAndRetry()) return
        }
        onErrorMessage(errorMessage(error))
    }

    override fun onResults(results: Bundle?) {
        val text = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.trim()

        if (text.isNullOrBlank()) {
            onErrorMessage("No Hindi speech was recognized. Please try again.")
        } else {
            onResult(text)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
