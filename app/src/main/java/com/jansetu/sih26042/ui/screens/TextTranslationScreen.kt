package com.jansetu.sih26042.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jansetu.sih26042.ui.AppViewModelFactory
import com.jansetu.sih26042.ui.TranslationViewModel

private val demoPhrases = listOf(
    "नमस्कार विद्यार्थी",
    "मेरा कॉलेज है....",
    "हमारा राज्य झारखंड है।",
    "सॉफ्टवेयर क्या है?"
)

@Composable
fun TextTranslationScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as Application
    val vm: TranslationViewModel = viewModel(factory = AppViewModelFactory(app))
    val s = vm.state

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenHeader("Hindi → Santhali", onBack)

        Text("Quick demo phrases", style = MaterialTheme.typography.titleSmall)
        demoPhrases.forEach { phrase ->
            AssistChip(
                onClick = { vm.onInput(phrase) },
                label = { Text(phrase) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            )
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = s.input,
            onValueChange = vm::onInput,
            label = { Text("Hindi classroom text") },
            supportingText = { Text("Source: Hindi • Output: Santhali in Ol Chiki") },
            modifier = Modifier.fillMaxWidth().height(160.dp)
        )

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = vm::translate,
            enabled = s.input.isNotBlank() && !s.busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (s.busy) {
                CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Translate to Ol Chiki")
            }
        }

        s.message?.let {
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth()) {
                Text(it, modifier = Modifier.padding(12.dp))
            }
        }

        if (s.output.isNotBlank()) {
            Spacer(Modifier.height(18.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Santhali (Ol Chiki)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(s.output, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (s.offline) {
                            "Offline • ${s.source}"
                        } else {
                            "${s.source} • ${s.latencyMs ?: 0} ms"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
