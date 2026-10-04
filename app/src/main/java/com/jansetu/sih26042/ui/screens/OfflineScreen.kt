package com.jansetu.sih26042.ui.screens

import android.app.Application
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
    val app = LocalContext.current.applicationContext as Application
    val vm: OfflineViewModel = viewModel(factory = AppViewModelFactory(app))
    val s = vm.state
    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader("Offline translation library", onBack)
        Text("Translations stored on this tablet: ${s.count}")
        Spacer(Modifier.height(10.dp))
        Text(
            "JanSetu can sync the complete pre-translated Hindi vocabulary library from the backend. " +
                "All downloaded Hindi → Santhali pairs are stored in Room/SQLite and work without internet."
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
            "Words and sentences that are not in the local library still use the IndicTrans2 model " +
                "when the backend is reachable, then are cached for future offline use."
        )
    }
}
