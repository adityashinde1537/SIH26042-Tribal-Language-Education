package com.jansetu.sih26042.ui.screens

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jansetu.sih26042.ui.AppViewModelFactory
import com.jansetu.sih26042.ui.MaterialsViewModel

@Composable
fun MaterialsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val vm: MaterialsViewModel = viewModel(factory = AppViewModelFactory(app))
    val s = vm.state
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ScreenHeader("Bilingual learning materials", onBack)
        OutlinedTextField(
            s.title,
            vm::title,
            label = { Text("Worksheet title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            s.domain,
            vm::domain,
            label = { Text("NIPUN FLN domain (e.g. Vocabulary)") },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Examples: Oral Language Development, Vocabulary, Reading Comprehension, Writing, Foundational Numeracy.")
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            s.content,
            vm::content,
            label = { Text("Hindi prompts/terms — one per line") },
            modifier = Modifier.fillMaxWidth().height(180.dp)
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            Button(onClick = vm::worksheet, enabled = !s.busy, modifier = Modifier.weight(1f)) {
                Text("Worksheet")
            }
            Spacer(Modifier.padding(4.dp))
            Button(onClick = vm::flashcards, enabled = !s.busy, modifier = Modifier.weight(1f)) {
                Text("Flashcards")
            }
        }
        s.message?.let { Spacer(Modifier.height(10.dp)); Text(it) }
        s.worksheet?.let { sheet ->
            Spacer(Modifier.height(18.dp))
            Text(sheet.title)
            Text("NIPUN FLN domain: ${sheet.nipunDomain}")
            sheet.rows.forEach { Text("${it.number}. ${it.hindi}  →  ${it.santhali}") }
            Spacer(Modifier.height(10.dp))
            Button(onClick = {
                val shareText = buildString {
                    appendLine(sheet.title)
                    appendLine("NIPUN FLN domain: ${sheet.nipunDomain}")
                    sheet.rows.forEach { appendLine("${it.number}. ${it.hindi} → ${it.santhali}") }
                }
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }, "Share worksheet"))
            }) { Text("Share worksheet") }
        }
        if (s.cards.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Text("Visual flashcards")
            s.cards.forEach { card ->
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(card.visual)
                        Text(card.hindi)
                        Text(card.santhali)
                        Text("NIPUN: ${card.nipunDomain}")
                    }
                }
            }
        }
    }
}
