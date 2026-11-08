package com.henrique.objectifs.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Vert = Color(0xFF43A047)
val Rouge = Color(0xFFE53935)
val GrisContour = Color(0xFFB0B0B0)

private val Couleurs = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEFDC),
    onPrimaryContainer = Color(0xFF0B2E0D),
    secondaryContainer = Color(0xFFDCEFDC),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF3F4F3),
    surfaceContainer = Color(0xFFF6F7F6),
)

@Composable
fun ThemeObjectifs(contenu: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Couleurs, content = contenu)
}
