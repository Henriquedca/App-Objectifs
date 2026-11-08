package com.henrique.objectifs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.henrique.objectifs.data.Donnees
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periode
import com.henrique.objectifs.data.Periodes
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun EcranAccueil(
    donnees: Donnees,
    mois: YearMonth,
    aujourdhui: LocalDate,
    onCycler: (Objectif, String) -> Unit,
    onAppuiLong: (Objectif) -> Unit,
    modifier: Modifier = Modifier,
) {
    val semaines = Periodes.semainesDuMois(mois)
    val mensuels = donnees.mensuelsPour(mois)
    val cleMois = Periodes.cleMois(mois)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (donnees.objectifs.isEmpty()) {
            item {
                Text(
                    "Aucun objectif pour l'instant. Appuie sur + pour en ajouter un.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { LigneJours(Modifier.padding(horizontal = 12.dp)) }
        items(semaines, key = { it.toString() }) { lundi ->
            CarteSemaine(lundi, mois, aujourdhui, donnees, onCycler, onAppuiLong)
        }
        if (mensuels.isNotEmpty()) {
            item(key = "mois") {
                Carte {
                    Text(
                        "Objectifs du mois",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    mensuels.forEach { o ->
                        LigneObjectif(o.nom, onAppuiLong = { onAppuiLong(o) }) {
                            Pastille(donnees.etat(o, cleMois), Periode.MENSUEL) { onCycler(o, cleMois) }
                        }
                    }
                }
            }
        }
        if (donnees.objectifs.isNotEmpty()) {
            item {
                Text(
                    "1 clic : réussi · 2 clics : raté · 3 clics : effacer\nAppui long sur un objectif pour le supprimer",
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CarteSemaine(
    lundi: LocalDate,
    mois: YearMonth,
    aujourdhui: LocalDate,
    donnees: Donnees,
    onCycler: (Objectif, String) -> Unit,
    onAppuiLong: (Objectif) -> Unit,
) {
    val cle = Periodes.cleSemaine(lundi)
    val objectifs = donnees.hebdoPour(lundi)
    val enCours = !aujourdhui.isBefore(lundi) && !aujourdhui.isAfter(lundi.plusDays(6))
    Carte(miseEnAvant = enCours) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (enCours) "Cette semaine" else "Semaine",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            for (i in 0..6) {
                val jour = lundi.plusDays(i.toLong())
                NumeroJour(jour, dansLeMois = mois == YearMonth.from(jour), estAujourdhui = jour == aujourdhui)
            }
        }
        objectifs.forEach { o ->
            LigneObjectif(o.nom, onAppuiLong = { onAppuiLong(o) }) {
                Pastille(donnees.etat(o, cle), Periode.HEBDO) { onCycler(o, cle) }
            }
        }
    }
}

@Composable
fun NumeroJour(jour: LocalDate, dansLeMois: Boolean, estAujourdhui: Boolean) {
    Box(Modifier.width(Cellule), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (estAujourdhui) MaterialTheme.colorScheme.primary else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = jour.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (estAujourdhui) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    estAujourdhui -> MaterialTheme.colorScheme.onPrimary
                    dansLeMois -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.outline
                },
            )
        }
    }
}

@Composable
fun Carte(miseEnAvant: Boolean = false, contenu: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (miseEnAvant) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) { contenu() }
    }
}
