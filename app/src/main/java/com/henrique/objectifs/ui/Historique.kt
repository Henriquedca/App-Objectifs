package com.henrique.objectifs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henrique.objectifs.data.Donnees
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periodes
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun EcranHistorique(
    donnees: Donnees,
    aujourdhui: LocalDate,
    onCycler: (Objectif, LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    var idChoisi by rememberSaveable { mutableStateOf<String?>(null) }
    var moisTexte by rememberSaveable { mutableStateOf(YearMonth.from(aujourdhui).toString()) }
    val mois = YearMonth.parse(moisTexte)
    val objectif = donnees.objectifs.firstOrNull { it.id == idChoisi } ?: donnees.objectifs.firstOrNull()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { EnTete(sousTitre = objectif?.nom ?: "", titre = "Historique") }

        if (objectif == null) {
            item { Text("Aucun objectif pour l'instant.", color = p.texteDoux, fontSize = 15.sp) }
            return@LazyColumn
        }

        if (donnees.objectifs.size > 1) {
            item {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    donnees.objectifs.forEach { o ->
                        val actif = o.id == objectif.id
                        Text(
                            o.nom,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (actif) p.vert else p.carte)
                                .clickable { idChoisi = o.id }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            color = if (actif) p.surVert else p.texte,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }

        item {
            Carte {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        Periodes.nomMois(mois),
                        color = p.texte,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).clickable { moisTexte = YearMonth.from(aujourdhui).toString() },
                    )
                    BoutonFleche(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mois précédent") {
                        moisTexte = mois.minusMonths(1).toString()
                    }
                    Spacer(Modifier.size(10.dp))
                    BoutonFleche(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Mois suivant") {
                        moisTexte = mois.plusMonths(1).toString()
                    }
                }
                Spacer(Modifier.height(18.dp))
                Calendrier(objectif, mois, aujourdhui, donnees, onCycler)
                Spacer(Modifier.height(16.dp))
                Legende()
            }
        }

        item {
            Carte {
                val semaines = Periodes.semainesDuMois(mois).reversed()
                semaines.forEachIndexed { i, lundi ->
                    if (i > 0) HorizontalDivider(color = p.separateur)
                    LigneSemaine(objectif, lundi, aujourdhui, donnees)
                }
            }
        }
    }
}

@Composable
private fun LigneSemaine(objectif: Objectif, lundi: LocalDate, aujourdhui: LocalDate, donnees: Donnees) {
    val p = LocalPalette.current
    val jours = Periodes.joursSemaine(lundi)
    val reussis = donnees.reussis(objectif, jours)
    val terminee = jours.last().isBefore(aujourdhui)
    Row(
        Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Sem. ${Periodes.libelleSemaine(lundi)}",
            color = p.texte,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        when {
            !terminee -> Badge("$reussis / 7", p.caseVide, p.texteDoux)
            reussis == 7 -> Badge("7 / 7", p.badgeVertFond, p.badgeVertTexte)
            else -> Badge("$reussis / 7", p.badgeOrangeFond, p.badgeOrangeTexte)
        }
    }
}

@Composable
private fun BoutonFleche(icone: ImageVector, description: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
            .border(1.dp, p.contourDoux, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, description, tint = p.texte)
    }
}
