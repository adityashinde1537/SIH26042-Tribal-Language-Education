package com.jansetu.sih26042.ui.screens

import android.Manifest
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jansetu.sih26042.ui.AppViewModelFactory
import com.jansetu.sih26042.ui.LibraryViewModel
import java.util.Locale

@Composable
fun LibraryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val vm: LibraryViewModel = viewModel(factory = AppViewModelFactory(app))
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

    DisposableEffect(Unit) {
        onDispose { tts.shutdown() }
    }

    fun speakSantali(text: String) {
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
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jansetu-library-santali")
    }

    val recognizer = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val recognized = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (recognized.isNullOrBlank()) {
            vm.voiceFailure("No Hindi speech was recognized. Try again.")
        } else {
            vm.voiceSearch(recognized)
        }
    }

    fun launchHindiRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a Hindi word or phrase")
        }
        try {
            recognizer.launch(intent)
        } catch (_: ActivityNotFoundException) {
            vm.voiceFailure("Hindi speech recognition is not available on this device.")
        }
    }

    val microphonePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchHindiRecognition()
        } else {
            vm.voiceFailure("Microphone permission is required for voice library mode.")
        }
    }

    LaunchedEffect(Unit) { vm.refresh() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader("Translation library", onBack)

        Text(
            "Hindi → Santali (Ol Chiki)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            s.totalStored.toString() + " translated entries stored offline on this device",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Type or speak Hindi. JanSetu searches the local library first; if missing, it translates through the backend and saves the result offline.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = s.query,
            onValueChange = vm::query,
            label = { Text("Search Hindi word or phrase") },
            placeholder = { Text("उदाहरण: धन्यवाद") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        launchHindiRecognition()
                    } else {
                        microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                enabled = !s.busy,
                modifier = Modifier.weight(1f)
            ) {
                Text("🎤 Speak Hindi")
            }

            Spacer(Modifier.padding(4.dp))

            Button(
                onClick = vm::translateAndSave,
                enabled = !s.busy && s.query.isNotBlank() && s.items.isEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Translate & save")
            }
        }

        Spacer(Modifier.height(12.dp))
        if (s.busy) {
            CircularProgressIndicator()
            Spacer(Modifier.height(8.dp))
        }

        s.message?.let {
            Card(Modifier.fillMaxWidth()) {
                Text(it, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(8.dp))
        }

        ttsMessage.value?.let {
            Card(Modifier.fillMaxWidth()) {
                Text(it, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = s.items, key = { it.hindi }) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            item.hindi,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            item.santhali,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { speakSantali(item.santhali) },
                            enabled = ttsReady.value,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🔊 Speak Santali")
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Stored offline • Source: " + item.engine,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
