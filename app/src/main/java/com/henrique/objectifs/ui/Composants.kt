package com.henrique.objectifs.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henrique.objectifs.data.Donnees
import com.henrique.objectifs.data.Etat
import com.henrique.objectifs.data.Moment
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periode
import com.henrique.objectifs.data.Periodes
import com.henrique.objectifs.data.moment
import java.time.LocalDate
import java.time.YearMonth

/**
 * Pastille d'un jour : rond (hebdo) ou carré (mensuel).
 * Vide passé = contour fin, aujourd'hui = anneau épais, futur = pointillés.
 */
@Composable
fun Pastille(periode: Periode, etat: Etat, moment: Moment, taille: Dp, onClick: (() -> Unit)? = null) {
    val p = LocalPalette.current
    val rond = periode == Periode.HEBDO
    val shape = if (rond) CircleShape else RoundedCornerShape(taille * 0.28f)
    var m = Modifier.size(taille).clip(shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    m = when (etat) {
        Etat.REUSSI -> m.background(p.vert)
        Etat.RATE -> m.background(p.rouge)
        Etat.VIDE -> when (moment) {
            Moment.AUJOURDHUI -> m.border(2.5.dp, p.contour, shape)
            Moment.PASSE -> m.border(1.5.dp, p.contourDoux, shape)
            Moment.FUTUR -> m.pointilles(p.contourDoux, rond)
        }
    }
    Box(m, contentAlignment = Alignment.Center) {
        if (taille >= 24.dp) {
            when (etat) {
                Etat.REUSSI -> Icon(Icons.Filled.Check, null, tint = p.surVert, modifier = Modifier.size(taille * 0.5f))
                Etat.RATE -> Icon(Icons.Filled.Close, null, tint = p.surRouge, modifier = Modifier.size(taille * 0.5f))
                Etat.VIDE -> {}
            }
        }
    }
}

private fun Modifier.pointilles(couleur: Color, rond: Boolean): Modifier = drawBehind {
    val w = 1.5.dp.toPx()
    val effet = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))
    if (rond) {
        drawCircle(couleur, radius = size.minDimension / 2 - w / 2, style = Stroke(w, pathEffect = effet))
    } else {
        drawRoundRect(
            couleur,
            topLeft = Offset(w / 2, w / 2),
            size = Size(size.width - w, size.height - w),
            cornerRadius = CornerRadius(size.minDimension * 0.28f),
            style = Stroke(w, pathEffect = effet),
        )
    }
}

/** Carte blanche arrondie ; appui long = supprimer. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Carte(onAppuiLong: (() -> Unit)? = null, contenu: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    var m = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(p.carte)
    if (onAppuiLong != null) m = m.combinedClickable(onClick = {}, onLongClick = onAppuiLong)
    Column(m.padding(20.dp), content = contenu)
}

@Composable
fun LettresJours(modifier: Modifier = Modifier, espacement: Dp = 0.dp) {
    val p = LocalPalette.current
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(espacement)) {
        Periodes.LETTRES_JOURS.forEach {
            Text(
                it,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = p.texteDoux,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun Badge(texte: String, fond: Color, couleur: Color) {
    Text(
        texte,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(fond).padding(horizontal = 12.dp, vertical = 5.dp),
        color = couleur,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun BarreProgression(fraction: Float, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Box(modifier.height(5.dp).clip(RoundedCornerShape(50)).background(p.contourDoux)) {
        if (fraction > 0f) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).clip(RoundedCornerShape(50)).background(p.vert))
        }
    }
}

/** Segmenté « Semaine | Mois ». */
@Composable
fun Segmente(options: List<String>, selection: Int, onSelection: (Int) -> Unit) {
    val p = LocalPalette.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(p.segmentFond).padding(5.dp)
    ) {
        options.forEachIndexed { i, texte ->
            val actif = i == selection
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .background(if (actif) p.segmentActif else Color.Transparent)
                    .clickable { onSelection(i) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    texte,
                    color = if (actif) p.texte else p.texteDoux,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

/** Calendrier du mois : cases colorées (vert réussi, rouge raté), cliquables jusqu'à aujourd'hui. */
@Composable
fun Calendrier(
    objectif: Objectif,
    mois: YearMonth,
    aujourdhui: LocalDate,
    donnees: Donnees,
    onCycler: (Objectif, LocalDate) -> Unit,
) {
    val p = LocalPalette.current
    val espace = 7.dp
    Column(verticalArrangement = Arrangement.spacedBy(espace)) {
        LettresJours(espacement = espace)
        Periodes.semainesDuMois(mois).forEach { lundi ->
            Row(horizontalArrangement = Arrangement.spacedBy(espace)) {
                Periodes.joursSemaine(lundi).forEach { jour ->
                    if (YearMonth.from(jour) != mois) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val m = moment(jour, aujourdhui)
                        val etat = donnees.etat(objectif, jour)
                        val (fond, texte) = when (etat) {
                            Etat.REUSSI -> p.vert to p.surVert
                            Etat.RATE -> p.rouge to p.surRouge
                            Etat.VIDE -> if (m == Moment.FUTUR) Color.Transparent to p.texteDoux else p.caseVide to p.texte
                        }
                        val forme = RoundedCornerShape(10.dp)
                        var modif = Modifier.weight(1f).aspectRatio(1.1f).clip(forme).background(fond)
                        if (m == Moment.AUJOURDHUI && etat == Etat.VIDE) modif = modif.border(2.dp, p.contour, forme)
                        if (m != Moment.FUTUR) modif = modif.clickable { onCycler(objectif, jour) }
                        Box(modif, contentAlignment = Alignment.Center) {
                            Text(jour.dayOfMonth.toString(), color = texte, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Legende() {
    val p = LocalPalette.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(p.vert))
        Text("  Réussi", color = p.texteDoux, fontSize = 14.sp)
        Spacer(Modifier.width(20.dp))
        Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(p.rouge))
        Text("  Raté", color = p.texteDoux, fontSize = 14.sp)
    }
}

/** Icônes de la barre de navigation (cible, histogramme). */
val IconeCible: ImageVector by lazy {
    ImageVector.Builder("cible", 24.dp, 24.dp, 24f, 24f).apply {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(21f, 12f); arcTo(9f, 9f, 0f, true, true, 3f, 12f); arcTo(9f, 9f, 0f, true, true, 21f, 12f); close()
        }
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(16f, 12f); arcTo(4f, 4f, 0f, true, true, 8f, 12f); arcTo(4f, 4f, 0f, true, true, 16f, 12f); close()
        }
    }.build()
}

val IconeHistogramme: ImageVector by lazy {
    ImageVector.Builder("histogramme", 24.dp, 24.dp, 24f, 24f).apply {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round) {
            moveTo(3f, 20.5f); lineTo(21f, 20.5f)
            moveTo(7f, 17f); lineTo(7f, 10f)
            moveTo(12f, 17f); lineTo(12f, 4f)
            moveTo(17f, 17f); lineTo(17f, 12f)
        }
    }.build()
}
