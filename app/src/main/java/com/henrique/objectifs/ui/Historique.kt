package com.henrique.objectifs.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.henrique.objectifs.data.Donnees
import com.henrique.objectifs.data.Etat
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periode
import com.henrique.objectifs.data.Periodes
import java.time.LocalDate
import java.time.YearMonth

private enum class Temps { PASSE, EN_COURS, A_VENIR }

@Composable
fun EcranHistorique(donnees: Donnees, mois: YearMonth, aujourdhui: LocalDate, modifier: Modifier = Modifier) {
    val semaines = Periodes.semainesDuMois(mois)
    val mensuels = donnees.mensuelsPour(mois)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Carte { GrilleCalendrier(semaines, mois, aujourdhui, donnees) } }
        item {
            Text(
                "Bilan par semaine",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(semaines, key = { it.toString() }) { lundi ->
            val dimanche = lundi.plusDays(6)
            val temps = when {
                dimanche.isBefore(aujourdhui) -> Temps.PASSE
                lundi.isAfter(aujourdhui) -> Temps.A_VENIR
                else -> Temps.EN_COURS
            }
            val cle = Periodes.cleSemaine(lundi)
            CarteBilan(
                titre = Periodes.libelleSemaine(lundi).replaceFirstChar { it.uppercase() },
                lignes = donnees.hebdoPour(lundi).map { it to donnees.etat(it, cle) },
                temps = temps,
            )
        }
        if (mensuels.isNotEmpty()) {
            item {
                val maintenant = YearMonth.from(aujourdhui)
                val temps = when {
                    mois.isBefore(maintenant) -> Temps.PASSE
                    mois.isAfter(maintenant) -> Temps.A_VENIR
                    else -> Temps.EN_COURS
                }
                val cle = Periodes.cleMois(mois)
                CarteBilan(
                    titre = "Bilan du mois",
                    lignes = mensuels.map { it to donnees.etat(it, cle) },
                    temps = temps,
                )
            }
        }
    }
}

@Composable
private fun GrilleCalendrier(semaines: List<LocalDate>, mois: YearMonth, aujourdhui: LocalDate, donnees: Donnees) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Periodes.LETTRES_JOURS.forEach { lettre ->
                Text(
                    lettre,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        semaines.forEach { lundi ->
            val cle = Periodes.cleSemaine(lundi)
            val hebdo = donnees.hebdoPour(lundi)
            Row(Modifier.fillMaxWidth()) {
                for (i in 0..6) {
                    val jour = lundi.plusDays(i.toLong())
                    CelluleJour(
                        jour = jour,
                        dansLeMois = YearMonth.from(jour) == mois,
                        estAujourdhui = jour == aujourdhui,
                        // Résumé de la semaine sous le dimanche
                        pastilles = if (i == 6) hebdo.map { donnees.etat(it, cle) } else emptyList(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CelluleJour(
    jour: LocalDate,
    dansLeMois: Boolean,
    estAujourdhui: Boolean,
    pastilles: List<Etat>,
    modifier: Modifier,
) {
    val contour = if (estAujourdhui) {
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
    } else Modifier
    Column(
        modifier = modifier.height(48.dp).padding(2.dp).then(contour).padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            jour.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (estAujourdhui) FontWeight.Bold else FontWeight.Normal,
            color = if (dansLeMois) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
        )
        if (pastilles.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                pastilles.take(4).forEach { Pastille(it, Periode.HEBDO, taille = 7.dp) }
            }
        }
    }
}

@Composable
private fun CarteBilan(titre: String, lignes: List<Pair<Objectif, Etat>>, temps: Temps) {
    Carte(miseEnAvant = temps == Temps.EN_COURS) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(titre, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                resume(lignes.map { it.second }, temps),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (lignes.isEmpty()) {
            Text(
                "Aucun objectif",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        lignes.forEach { (objectif, etat) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(22.dp), contentAlignment = Alignment.Center) {
                    Pastille(etat, objectif.periode, taille = 14.dp)
                }
                Spacer(Modifier.width(8.dp))
                Text(objectif.nom, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(
                    libelle(etat, temps),
                    style = MaterialTheme.typography.bodySmall,
                    color = when (etat) {
                        Etat.REUSSI -> Vert
                        Etat.RATE -> Rouge
                        Etat.VIDE -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

private fun libelle(etat: Etat, temps: Temps): String = when (etat) {
    Etat.REUSSI -> "Réussi"
    Etat.RATE -> "Raté"
    Etat.VIDE -> when (temps) {
        Temps.PASSE -> "Oubli"
        Temps.EN_COURS -> "En cours"
        Temps.A_VENIR -> "À venir"
    }
}

private fun resume(etats: List<Etat>, temps: Temps): String {
    if (etats.isEmpty()) return ""
    val reussis = etats.count { it == Etat.REUSSI }
    val rates = etats.count { it == Etat.RATE }
    val vides = etats.count { it == Etat.VIDE }
    val parties = mutableListOf("$reussis/${etats.size} réussi" + if (reussis > 1) "s" else "")
    if (rates > 0) parties += "$rates raté" + if (rates > 1) "s" else ""
    if (vides > 0 && temps == Temps.PASSE) parties += "$vides oubli" + if (vides > 1) "s" else ""
    return parties.joinToString(" · ")
}

