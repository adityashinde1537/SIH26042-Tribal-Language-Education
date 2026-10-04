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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jansetu.sih26042.ui.AppViewModelFactory
import com.jansetu.sih26042.ui.VoiceViewModel
import com.jansetu.sih26042.ui.speech.HindiSpeechRecognizer
import java.util.Locale

@Composable
fun VoiceTranslationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val vm: VoiceViewModel = viewModel(factory = AppViewModelFactory(app))
    val s = vm.state

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
            speech.startListening()
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
            speech.startListening()
        } else {
            permission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun speakSantali() {
        if (!ttsReady.value) {
            ttsMessage.value = "Santali speech is not ready on this device."
            return
        }

        val languageResult = tts.setLanguage(Locale("sat", "IN"))
        if (
            languageResult == TextToSpeech.LANG_MISSING_DATA ||
            languageResult == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            ttsMessage.value = "Santali TTS voice data is not installed on this device."
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
                    "Speech input: " + speech.modeLabel,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    if (speech.isAvailable) {
                        "JanSetu now listens inside the app instead of opening Google Voice Search."
                    } else {
                        "Speech service is unavailable; Hindi text translation still works."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = ::startHindiSpeech,
            enabled = !s.busy && speech.isAvailable,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (s.busy) "Listening / translating…" else "🎤 Speak Hindi")
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
                enabled = ttsReady.value,
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
