package com.jansetu.sih26042.ui.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.jansetu.sih26042.ui.speech.HindiAsrModelPack
import com.jansetu.sih26042.ui.speech.HindiOfflineRecognizer
import com.jansetu.sih26042.ui.speech.SantaliTtsPack
import com.jansetu.sih26042.ui.speech.PiperSantaliVoice
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jansetu.sih26042.ui.AppViewModelFactory
import com.jansetu.sih26042.ui.VoiceViewModel
import com.jansetu.sih26042.ui.speech.HindiSpeechRecognizer

@Composable
fun VoiceTranslationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val vm: VoiceViewModel = viewModel(factory = AppViewModelFactory(app))
    val s = vm.state
    val hindiPack = remember(context) { HindiAsrModelPack(context) }
    val offlineSpeech = remember(context) { HindiOfflineRecognizer(context, hindiPack) }
    val captureScope = rememberCoroutineScope()
    val recording = remember { mutableStateOf(false) }
    val stopRequested = remember { mutableStateOf(false) }
    val voicePack = remember(context) { SantaliTtsPack(context) }
    val piper = remember(context) { PiperSantaliVoice(voicePack) }
    val speaking = remember { mutableStateOf(false) }

    fun captureUsingOfflineModel() {
        if (recording.value) { stopRequested.value = true; return }
        recording.value = true
        stopRequested.value = false
        vm.beginCycle()
        captureScope.launch {
            val result = runCatching { offlineSpeech.listen { stopRequested.value } }
            recording.value = false
            result.onSuccess(vm::recognized)
                .onFailure { vm.fail(it.message ?: "Offline Hindi transcription failed.") }
        }
    }

    val ttsReady = remember { mutableStateOf(false) }
    val ttsMessage = remember { mutableStateOf<String?>(null) }
    val tts = remember {
        TextToSpeech(context) { status ->
            ttsReady.value = status == TextToSpeech.SUCCESS
            if (status != TextToSpeech.SUCCESS) {
                ttsMessage.value = "Android text-to-speech could not start."
            }
        }
    }

    val speech = remember(context) {
        HindiSpeechRecognizer(
            context = context,
            onListening = vm::beginCycle,
            onResult = vm::recognized,
            onErrorMessage = vm::fail
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            speech.destroy()
            tts.shutdown()
        }
    }

    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (hindiPack.ready()) captureUsingOfflineModel() else speech.startListening()
        } else {
            vm.fail("Microphone permission is required for voice mode.")
        }
    }

    fun startHindiSpeech() {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (hindiPack.ready()) captureUsingOfflineModel() else speech.startListening()
        } else {
            permission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun speakSantali() {
        if (voicePack.ready()) {
            speaking.value = true
            ttsMessage.value = "Generating local Santhali speech…"
            captureScope.launch {
                runCatching { piper.speak(s.santhali) }
                    .onSuccess { ttsMessage.value = null }
                    .onFailure { ttsMessage.value = "Offline voice error: " + (it.message ?: "unknown") }
                speaking.value = false
            }
            return
        }
        if (!ttsReady.value) {
            ttsMessage.value = "Santali speech is not ready on this device."
            return
        }

        // No network audio: only voices declared local by the installed TTS engine.
        val offlineSantaliVoice = tts.voices?.firstOrNull { voice ->
            voice.locale.language.equals("sat", ignoreCase = true) &&
                !voice.isNetworkConnectionRequired
        }
        if (offlineSantaliVoice == null || tts.setVoice(offlineSantaliVoice) != TextToSpeech.SUCCESS) {
            ttsMessage.value = "An offline Santhali voice is not installed. Ol Chiki text remains available."
            return
        }

        ttsMessage.value = null
        tts.speak(
            s.santhali,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jansetu-voice-output"
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader("Voice classroom mode", onBack)

        Text(
            "Hindi speech → translation → Santhali text/audio.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(8.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    "Speech input: " + if (hindiPack.ready()) "Offline Hindi CTC speech model" else speech.modeLabel,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    if (hindiPack.ready()) {
                        "Hindi microphone transcription runs entirely on the phone. Tap Stop to finish speaking."
                    } else if (speech.isAvailable) {
                        "JanSetu accepts only installed on-device Hindi recognition. It never sends classroom audio to a cloud fallback."
                    } else {
                        "Speech service is unavailable; Hindi text translation still works."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { if (recording.value) stopRequested.value = true else startHindiSpeech() },
            enabled = recording.value || (!s.busy && (hindiPack.ready() || speech.isAvailable)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (recording.value) "■ Stop recording" else if (s.busy) "Translating…" else "🎤 Speak Hindi")
        }

        s.message?.let {
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth()) {
                Text(it, modifier = Modifier.padding(12.dp))
            }
        }

        if (s.recognizedHindi.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text("Hindi", style = MaterialTheme.typography.titleMedium)
            Text(s.recognizedHindi)
        }

        if (s.santhali.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text("Santhali (Ol Chiki)", style = MaterialTheme.typography.titleMedium)
            Text(s.santhali, style = MaterialTheme.typography.headlineSmall)

            s.roundTripMs?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Speech-start → translation: " + it + " ms" +
                        if (it <= 3000) " ✓ target" else " (above 3 s target)"
                )
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = ::speakSantali,
                enabled = !speaking.value && (voicePack.ready() || ttsReady.value),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔊 Speak Santali output")
            }
        }

        ttsMessage.value?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
    }
}
