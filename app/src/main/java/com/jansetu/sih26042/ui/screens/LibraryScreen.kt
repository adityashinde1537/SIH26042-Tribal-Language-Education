package com.jansetu.sih26042.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jansetu.sih26042.ui.AppViewModelFactory
import com.jansetu.sih26042.ui.LibraryViewModel

@Composable
fun LibraryScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as Application
    val vm: LibraryViewModel = viewModel(factory = AppViewModelFactory(app))
    val s = vm.state

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
            "Search stored translations. If a Hindi term is missing, translate it through the backend and save it for offline use.",
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
        Button(
            onClick = vm::translateAndSave,
            enabled = !s.busy && s.query.isNotBlank() && s.items.isEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Translate & save to offline library")
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

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = s.items, key = { it.hindi }) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(item.hindi, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(item.santhali, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        Text("Stored offline • Source: " + item.engine, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
