package com.henrique.objectifs.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.henrique.objectifs.data.Etat
import com.henrique.objectifs.data.Periode
import com.henrique.objectifs.data.Periodes

/** Largeur d'une colonne de jour ; la pastille est dans la dernière (« D »). */
val Cellule = 30.dp

fun forme(periode: Periode): Shape =
    if (periode == Periode.HEBDO) CircleShape else RoundedCornerShape(4.dp)

/** Pastille : vide (contour gris), verte (réussi) ou rouge (raté). */
@Composable
fun Pastille(etat: Etat, periode: Periode, taille: Dp = 24.dp, onClick: (() -> Unit)? = null) {
    val shape = forme(periode)
    val fond = when (etat) {
        Etat.VIDE -> Color.Transparent
        Etat.REUSSI -> Vert
        Etat.RATE -> Rouge
    }
    var modifier = Modifier
        .size(taille)
        .clip(shape)
        .background(fond)
    if (etat == Etat.VIDE) modifier = modifier.border(2.dp, GrisContour, shape)
    if (onClick != null) modifier = modifier.clickable(onClick = onClick)
    Box(modifier)
}

/** Ligne « L M M J V S D » alignée sur les colonnes des cartes. */
@Composable
fun LigneJours(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth()) {
        Spacer(Modifier.weight(1f))
        Periodes.LETTRES_JOURS.forEach { lettre ->
            Text(
                text = lettre,
                modifier = Modifier.width(Cellule),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Nom de l'objectif à gauche, contenu (pastille) dans la colonne « D ». Appui long = supprimer. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LigneObjectif(nom: String, onAppuiLong: () -> Unit, colonneD: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onAppuiLong)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(nom, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Box(Modifier.width(Cellule), contentAlignment = Alignment.Center) {
            Row(content = colonneD)
        }
    }
}
