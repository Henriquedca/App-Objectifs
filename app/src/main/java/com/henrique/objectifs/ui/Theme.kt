package com.henrique.objectifs.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class Palette(
    val fond: Color,
    val carte: Color,
    val texte: Color,
    val texteDoux: Color,
    val vert: Color,
    val surVert: Color,
    val rouge: Color,
    val surRouge: Color,
    /** Anneau épais du jour courant. */
    val contour: Color,
    /** Contours des jours passés/futurs, pistes. */
    val contourDoux: Color,
    /** Case vide (jour passé) dans les calendriers. */
    val caseVide: Color,
    val segmentFond: Color,
    val segmentActif: Color,
    val badgeVertFond: Color,
    val badgeVertTexte: Color,
    val badgeOrangeFond: Color,
    val badgeOrangeTexte: Color,
    val separateur: Color,
)

val PaletteClaire = Palette(
    fond = Color(0xFFF3F2EE),
    carte = Color(0xFFFFFFFF),
    texte = Color(0xFF16181D),
    texteDoux = Color(0xFF6B6E73),
    vert = Color(0xFF1F6B4F),
    surVert = Color(0xFFFFFFFF),
    rouge = Color(0xFFC2410C),
    surRouge = Color(0xFFFFFFFF),
    contour = Color(0xFF16181D),
    contourDoux = Color(0xFFC4C2BC),
    caseVide = Color(0xFFEDEBE6),
    segmentFond = Color(0xFFE6E4DF),
    segmentActif = Color(0xFFFFFFFF),
    badgeVertFond = Color(0xFFDCEFE5),
    badgeVertTexte = Color(0xFF1F6B4F),
    badgeOrangeFond = Color(0xFFFCE3D6),
    badgeOrangeTexte = Color(0xFF9A3412),
    separateur = Color(0xFFECEAE5),
)

val PaletteSombre = Palette(
    fond = Color(0xFF111317),
    carte = Color(0xFF1C1F25),
    texte = Color(0xFFF4F5F7),
    texteDoux = Color(0xFF9AA0A8),
    vert = Color(0xFF4ADE80),
    surVert = Color(0xFF0B2E17),
    rouge = Color(0xFFE5484D),
    surRouge = Color(0xFFFFFFFF),
    contour = Color(0xFFFFFFFF),
    contourDoux = Color(0xFF3B4049),
    caseVide = Color(0xFF262A31),
    segmentFond = Color(0xFF1C1F25),
    segmentActif = Color(0xFF2F333B),
    badgeVertFond = Color(0xFF173A26),
    badgeVertTexte = Color(0xFF4ADE80),
    badgeOrangeFond = Color(0xFF3D2216),
    badgeOrangeTexte = Color(0xFFFF9B6B),
    separateur = Color(0xFF2A2E35),
)

val LocalPalette = staticCompositionLocalOf { PaletteClaire }

@Composable
fun ThemeObjectifs(contenu: @Composable () -> Unit) {
    val sombre = isSystemInDarkTheme()
    val p = if (sombre) PaletteSombre else PaletteClaire
    val schema = if (sombre) {
        darkColorScheme(
            primary = p.vert, onPrimary = p.surVert,
            background = p.fond, onBackground = p.texte,
            surface = p.carte, onSurface = p.texte, onSurfaceVariant = p.texteDoux,
            surfaceContainerHigh = p.carte, surfaceContainer = p.carte,
            outline = p.contourDoux, error = p.rouge,
        )
    } else {
        lightColorScheme(
            primary = p.vert, onPrimary = p.surVert,
            background = p.fond, onBackground = p.texte,
            surface = p.carte, onSurface = p.texte, onSurfaceVariant = p.texteDoux,
            surfaceContainerHigh = p.carte, surfaceContainer = p.carte,
            outline = p.contourDoux, error = p.rouge,
        )
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = schema, content = contenu)
    }
}
