package com.henrique.objectifs.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henrique.objectifs.data.Donnees
import com.henrique.objectifs.data.Moment
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periode
import com.henrique.objectifs.data.Periodes
import com.henrique.objectifs.data.moment
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun EcranObjectifs(
    donnees: Donnees,
    aujourdhui: LocalDate,
    onCycler: (Objectif, LocalDate) -> Unit,
    onAjouter: (Periode) -> Unit,
    onSupprimer: (Objectif) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    var onglet by rememberSaveable { mutableIntStateOf(0) }
    var lundiTexte by rememberSaveable { mutableStateOf(Periodes.lundi(aujourdhui).toString()) }
    var moisTexte by rememberSaveable { mutableStateOf(YearMonth.from(aujourdhui).toString()) }
    val lundi = LocalDate.parse(lundiTexte)
    val mois = YearMonth.parse(moisTexte)

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                EnTete(
                    sousTitre = if (onglet == 0) "Semaine ${Periodes.libelleSemaine(lundi)}" else Periodes.nomMois(mois),
                    titre = "Mes objectifs",
                    onPrecedent = {
                        if (onglet == 0) lundiTexte = lundi.minusWeeks(1).toString()
                        else moisTexte = mois.minusMonths(1).toString()
                    },
                    onSuivant = {
                        if (onglet == 0) lundiTexte = lundi.plusWeeks(1).toString()
                        else moisTexte = mois.plusMonths(1).toString()
                    },
                    onAujourdhui = {
                        lundiTexte = Periodes.lundi(aujourdhui).toString()
                        moisTexte = YearMonth.from(aujourdhui).toString()
                    },
                )
            }
            item { Segmente(listOf("Semaine", "Mois"), onglet) { onglet = it } }

            val liste = if (onglet == 0) donnees.hebdo else donnees.mensuels
            if (liste.isEmpty()) {
                item {
                    Text(
                        if (onglet == 0) "Aucun objectif de la semaine.\nAppuie sur + pour en ajouter un."
                        else "Aucun objectif du mois.\nAppuie sur + pour en ajouter un.",
                        color = p.texteDoux,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    )
                }
            }
            items(liste, key = { it.id }) { o ->
                if (onglet == 0) CarteHebdo(o, lundi, aujourdhui, donnees, onCycler, onSupprimer)
                else CarteMensuel(o, mois, aujourdhui, donnees, onCycler, onSupprimer)
            }
            if (liste.isNotEmpty()) {
                item {
                    Text(
                        "Appui long sur un objectif pour le supprimer",
                        color = p.texteDoux,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { onAjouter(if (onglet == 0) Periode.HEBDO else Periode.MENSUEL) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = p.vert,
            contentColor = p.surVert,
            shape = RoundedCornerShape(22.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Ajouter un objectif")
        }
    }
}

@Composable
fun EnTete(
    sousTitre: String,
    titre: String,
    onPrecedent: (() -> Unit)? = null,
    onSuivant: (() -> Unit)? = null,
    onAujourdhui: (() -> Unit)? = null,
) {
    val p = LocalPalette.current
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                sousTitre,
                color = p.texteDoux,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f).let { if (onAujourdhui != null) it.clickable(onClick = onAujourdhui) else it },
            )
            if (onPrecedent != null) {
                IconButton(onClick = onPrecedent) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Précédent", tint = p.texteDoux)
                }
            }
            if (onSuivant != null) {
                IconButton(onClick = onSuivant) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Suivant", tint = p.texteDoux)
                }
            }
        }
        Text(titre, color = p.texte, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp)
    }
}

@Composable
private fun CarteHebdo(
    objectif: Objectif,
    lundi: LocalDate,
    aujourdhui: LocalDate,
    donnees: Donnees,
    onCycler: (Objectif, LocalDate) -> Unit,
    onSupprimer: (Objectif) -> Unit,
) {
    val p = LocalPalette.current
    Carte(onAppuiLong = { onSupprimer(objectif) }) {
        Text(objectif.nom, color = p.texte, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LettresJours()
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Periodes.joursSemaine(lundi).forEach { jour ->
                val m = moment(jour, aujourdhui)
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Pastille(
                        periode = Periode.HEBDO,
                        etat = donnees.etat(objectif, jour),
                        moment = m,
                        taille = 38.dp,
                        onClick = if (m == Moment.FUTUR) null else ({ onCycler(objectif, jour) }),
                    )
                }
            }
        }
    }
}

@Composable
private fun CarteMensuel(
    objectif: Objectif,
    mois: YearMonth,
    aujourdhui: LocalDate,
    donnees: Donnees,
    onCycler: (Objectif, LocalDate) -> Unit,
    onSupprimer: (Objectif) -> Unit,
) {
    val p = LocalPalette.current
    val total = mois.lengthOfMonth()
    val reussis = donnees.reussisDuMois(objectif, mois)
    val moisCourant = mois == YearMonth.from(aujourdhui)
    Carte(onAppuiLong = { onSupprimer(objectif) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "OBJECTIF DE ${Periodes.nomMoisSeul(mois).uppercase()}",
                    color = p.texteDoux,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Text(objectif.nom, color = p.texte, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$reussis / $total j", color = p.texte, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                BarreProgression(reussis.toFloat() / total, Modifier.width(80.dp))
            }
            if (moisCourant) {
                Spacer(Modifier.width(16.dp))
                Pastille(
                    periode = Periode.MENSUEL,
                    etat = donnees.etat(objectif, aujourdhui),
                    moment = Moment.AUJOURDHUI,
                    taille = 40.dp,
                    onClick = { onCycler(objectif, aujourdhui) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Calendrier(objectif, mois, aujourdhui, donnees, onCycler)
    }
}
