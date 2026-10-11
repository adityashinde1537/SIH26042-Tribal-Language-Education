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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenHeader("Bilingual learning materials", onBack)

        Text(
            "Create a bilingual Hindi → Santali worksheet or visual flashcards.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = s.title,
            onValueChange = vm::title,
            label = { Text("Worksheet title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = s.domain,
            onValueChange = vm::domain,
            label = { Text("NIPUN FLN domain") },
            supportingText = {
                Text("Examples: Vocabulary, Reading Comprehension, Writing, Foundational Numeracy")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = s.content,
            onValueChange = vm::content,
            label = { Text("Hindi prompts — one per line") },
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        )

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = vm::worksheet,
                enabled = !s.busy,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (s.busy) "Generating…" else "Generate worksheet")
            }

            Spacer(Modifier.padding(4.dp))

            Button(
                onClick = vm::flashcards,
                enabled = !s.busy,
                modifier = Modifier.weight(1f)
            ) {
                Text("Generate flashcards")
            }
        }

        s.message?.let {
            Spacer(Modifier.height(10.dp))
            Card(Modifier.fillMaxWidth()) {
                Text(it, modifier = Modifier.padding(12.dp))
            }
        }

        s.worksheet?.let { sheet ->
            Spacer(Modifier.height(18.dp))

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Generated bilingual worksheet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(sheet.title, fontWeight = FontWeight.Bold)
                    Text("NIPUN FLN domain: " + sheet.nipunDomain)

                    Spacer(Modifier.height(12.dp))

                    sheet.rows.forEach { row ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    row.number.toString() + ". Hindi",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(row.hindi)

                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Santali (Ol Chiki)",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    row.santhali,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val shareText = buildString {
                                appendLine(sheet.title)
                                appendLine("NIPUN FLN domain: " + sheet.nipunDomain)
                                appendLine()
                                sheet.rows.forEach {
                                    appendLine(it.number.toString() + ". " + it.hindi)
                                    appendLine("   " + it.santhali)
                                    appendLine()
                                }
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    },
                                    "Share worksheet"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share worksheet")
                    }
                }
            }
        }

        if (s.cards.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Text(
                "Visual flashcards",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            s.cards.forEach { card ->
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(card.visual, style = MaterialTheme.typography.headlineMedium)
                        Text(card.hindi, fontWeight = FontWeight.Bold)
                        Text(
                            card.santhali,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "NIPUN: " + card.nipunDomain,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
