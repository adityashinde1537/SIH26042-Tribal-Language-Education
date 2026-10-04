package com.jansetu.sih26042.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onText: () -> Unit, onVoice: () -> Unit, onMaterials: () -> Unit, onOffline: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("JanSetu", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("AI-assisted vernacular pedagogy for Hindi-medium teachers and Santhali-speaking primary students.", textAlign = TextAlign.Center)
        Text("SIH26042 • Prototype language: Santhali (Ol Chiki)", style = MaterialTheme.typography.bodySmall)
        Button(onClick = onText, modifier = Modifier.fillMaxWidth()) { Text("Text translation") }
        Button(onClick = onVoice, modifier = Modifier.fillMaxWidth()) { Text("Voice-to-voice classroom mode") }
        Button(onClick = onMaterials, modifier = Modifier.fillMaxWidth()) { Text("Worksheets & flashcards") }
        Button(onClick = onOffline, modifier = Modifier.fillMaxWidth()) { Text("Offline content & sync") }
    }
}
