package com.jansetu.sih26042.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JanSetuColors = lightColorScheme(
    primary = Color(0xFF176B52),
    onPrimary = Color.White,
    secondary = Color(0xFF486A5D),
    tertiary = Color(0xFF8A5D16),
    surfaceVariant = Color(0xFFE4F1EA),
    secondaryContainer = Color(0xFFE8F0FF)
)

@Composable
fun JanSetuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JanSetuColors,
        content = content
    )
}
