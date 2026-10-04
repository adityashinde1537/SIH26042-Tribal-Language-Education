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
        ScreenHeader("Offline content", onBack)
        Text("Cached translations on this tablet: ${s.count}")
        Spacer(Modifier.height(10.dp))
        Text("Use initial connectivity to sync the starter pack and translate planned FLN lesson phrases. Those entries are stored in Room/SQLite and remain available without internet.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = vm::sync, enabled = !s.busy, modifier = Modifier.fillMaxWidth()) { Text(if (s.busy) "Syncing…" else "Sync starter content") }
        s.message?.let { Spacer(Modifier.height(12.dp)); Text(it) }
        Spacer(Modifier.height(20.dp))
        Text("Prototype limitation: arbitrary uncached offline translation still requires a future on-device model pack. This repository does not claim otherwise.")
    }
}
