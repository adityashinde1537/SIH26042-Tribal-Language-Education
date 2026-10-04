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
import androidx.compose.material3.Button
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

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader("Offline translation library", onBack)
        Text("Device evidence: Android API ${Build.VERSION.SDK_INT} • RAM ${totalRamMb} MB")
        Text("Translations stored on this tablet: ${s.count}")
        Spacer(Modifier.height(10.dp))
        Text(
            "JanSetu can sync the translated Hindi vocabulary library from the backend. " +
                "Downloaded Hindi → Santhali pairs are stored in Room/SQLite and work without internet."
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = vm::syncFullLexicon,
            enabled = !s.busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (s.busy) "Syncing…" else "Sync full translation library") }

        Spacer(Modifier.height(10.dp))
        Button(
            onClick = vm::syncStarter,
            enabled = !s.busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Sync small demo pack only") }

        s.message?.let { Spacer(Modifier.height(12.dp)); Text(it) }

        if (s.sourceTerms > 0) {
            Spacer(Modifier.height(12.dp))
            Text("Server lexicon: ${s.serverTranslated}/${s.sourceTerms} translated")
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Offline behavior: exact cached phrases work directly. If an unseen sentence is made only " +
                "from individually synced words, JanSetu can use a labeled word-composition fallback. " +
                "New words still need the IndicTrans2 backend until an on-device neural model pack is added."
        )
    }
}
