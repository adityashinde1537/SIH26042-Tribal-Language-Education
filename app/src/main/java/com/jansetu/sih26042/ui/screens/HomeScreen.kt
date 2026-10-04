package com.jansetu.sih26042.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onText: () -> Unit,
    onVoice: () -> Unit,
    onMaterials: () -> Unit,
    onLibrary: () -> Unit,
    onOffline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "JanSetu",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "AI-Powered Mother Tongue Classroom Bridge",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text("Hindi → Santhali (Ol Chiki)", style = MaterialTheme.typography.titleSmall)
                Text(
                    "SIH26042 • Team JanSetu • Working prototype",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            "Teacher tools",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        FeatureCard(
            icon = Icons.Filled.Translate,
            title = "Text translation",
            subtitle = "Translate Hindi classroom text into validated Ol Chiki.",
            action = "Open translator",
            onClick = onText
        )
        FeatureCard(
            icon = Icons.Filled.RecordVoiceOver,
            title = "Voice classroom",
            subtitle = "Speak Hindi, translate, measure latency and play Santali TTS when available.",
            action = "Start voice mode",
            onClick = onVoice
        )
        FeatureCard(
            icon = Icons.Filled.LibraryBooks,
            title = "Browse translation library",
            subtitle = "Search the Hindi → Santali translations actually stored on this device.",
            action = "Browse library",
            onClick = onLibrary
        )
        FeatureCard(
            icon = Icons.Filled.Description,
            title = "NIPUN learning materials",
            subtitle = "Generate bilingual worksheets and visual flashcards with FLN-domain tags.",
            action = "Create materials",
            onClick = onMaterials
        )
        FeatureCard(
            icon = Icons.Filled.CloudDownload,
            title = "Offline classroom library",
            subtitle = "Sync translations to Room/SQLite for use without connectivity.",
            action = "Manage offline library",
            onClick = onOffline
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "30-second judge demo",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text("1. Open Text translation and tap a demo phrase.")
                Text("2. Open Browse library and search a stored Hindi word.")
                Text("3. Show Offline library device API/RAM and sync status.")
                Text("4. Generate one NIPUN worksheet or flashcard.")
            }
        }

        Text(
            "Prototype note: synced/cached content works offline. Completely unseen words still require the backend until an on-device neural model pack is added.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.padding(5.dp))
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(6.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text(action)
            }
        }
    }
}
