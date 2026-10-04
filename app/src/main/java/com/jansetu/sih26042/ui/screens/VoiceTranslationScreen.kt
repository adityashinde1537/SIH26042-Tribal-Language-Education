package com.jansetu.sih26042.ui.screens

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
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
import java.util.Locale

@Composable
fun VoiceTranslationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val vm: VoiceViewModel = viewModel(factory = AppViewModelFactory(app))
    val s = vm.state
    val ttsReady = remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { status -> ttsReady.value = status == TextToSpeech.SUCCESS }
    }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val recognizer = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (text.isNullOrBlank()) vm.fail("No Hindi speech was recognized.") else vm.recognized(text)
    }

    fun launchRecognition() {
        vm.beginCycle()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Hindi")
        }
        recognizer.launch(intent)
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchRecognition() else vm.fail("Microphone permission is required for voice mode.")
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader("Voice classroom mode", onBack)
        Text("Hindi speech → translation → Santhali text/audio. Android is asked to prefer offline speech recognition when available.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) launchRecognition()
            else permission.launch(Manifest.permission.RECORD_AUDIO)
        }, enabled = !s.busy, modifier = Modifier.fillMaxWidth()) { Text(if (s.busy) "Working…" else "Speak Hindi") }
        s.message?.let { Spacer(Modifier.height(12.dp)); Text(it) }
        if (s.recognizedHindi.isNotBlank()) { Spacer(Modifier.height(16.dp)); Text("Hindi: ${s.recognizedHindi}") }
        if (s.santhali.isNotBlank()) {
            Spacer(Modifier.height(12.dp)); Text("Santhali: ${s.santhali}")
            s.roundTripMs?.let { Text("Measured speech-start → translation result: $it ms${if (it <= 3000) " ✓ target" else " (above 3 s target)"}") }
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                if (ttsReady.value) {
                    tts.language = Locale("sat", "IN")
                    tts.speak(s.santhali, TextToSpeech.QUEUE_FLUSH, null, "jansetu-output")
                }
            }, enabled = ttsReady.value, modifier = Modifier.fillMaxWidth()) { Text("Speak Santhali output") }
            Text("Audio availability depends on the installed Android TTS engine having Santhali voice data.")
        }
    }
}
