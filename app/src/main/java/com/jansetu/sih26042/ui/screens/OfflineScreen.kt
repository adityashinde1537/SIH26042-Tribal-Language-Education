package com.jansetu.sih26042.ui.screens

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jansetu.sih26042.ui.AppViewModelFactory
import com.jansetu.sih26042.ui.OfflineViewModel

@Composable
fun OfflineScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val vm: OfflineViewModel = viewModel(factory = AppViewModelFactory(app))
    val s = vm.state
    val memoryInfo = ActivityManager.MemoryInfo()
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.getMemoryInfo(memoryInfo)
    val totalRamMb = memoryInfo.totalMem / (1024L * 1024L)

    LaunchedEffect(Unit) { vm.refresh() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenHeader("Offline translation library", onBack)
        Text("Device evidence: Android API \${Build.VERSION.SDK_INT} • RAM \${totalRamMb} MB")
        Text("Translations stored on this device: \${s.count}")

        Spacer(Modifier.height(14.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    "Built-in presentation demo",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "The tested classroom examples are bundled inside the APK, so this demo does not need Wi-Fi, mobile data, or a running backend."
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = vm::syncStarter,
                    enabled = !s.busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Load built-in demo pack")
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Backend for full translation library", style = MaterialTheme.typography.titleMedium)
        Text(
            "10.0.2.2 works only in the Android emulator. On a real phone, use the LAN IP of the computer running FastAPI, for example http://192.168.1.5:8000/."
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = s.backendUrl,
            onValueChange = vm::backendUrl,
            label = { Text("Backend URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = vm::saveBackendUrl,
            enabled = !s.busy && s.backendUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save backend URL")
        }

        Spacer(Modifier.height(10.dp))
        Button(
            onClick = vm::syncFullLexicon,
            enabled = !s.busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (s.busy) "Working…" else "Sync full translation library")
        }

        s.message?.let {
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth()) {
                Text(it, modifier = Modifier.padding(12.dp))
            }
        }

        if (s.sourceTerms > 0) {
            Spacer(Modifier.height(12.dp))
            Text("Server lexicon: \${s.serverTranslated}/\${s.sourceTerms} translated")
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Offline behavior: bundled/cached phrases work directly. A sentence made only from individually synced words can use the labeled word-composition fallback. Completely unseen words still require the backend.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
