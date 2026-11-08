package com.henrique.objectifs.ui

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

/** Semaine et mois EN COURS uniquement ; les autres périodes se consultent dans l'Historique. */
@Composable
fun EcranObjectifs(
    donnees: Donnees,
    aujourdhui: LocalDate,
    onCycler: (Objectif, LocalDate) -> Unit,
    onAjouter: (Periode) -> Unit,
    onMenu: (Objectif) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    var onglet by rememberSaveable { mutableIntStateOf(0) }
    val lundi = Periodes.lundi(aujourdhui)
    val mois = YearMonth.from(aujourdhui)

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                EnTete(
                    sousTitre = if (onglet == 0) Periodes.libellePeriode(Periode.HEBDO, lundi) else Periodes.nomMois(mois),
                    titre = "Mes objectifs",
                )
            }
            item { Segmente(listOf("Hebdomadaire", "Mensuel"), onglet) { onglet = it } }

            val liste = if (onglet == 0) donnees.hebdoDe(lundi) else donnees.mensuelsDe(mois)
            if (liste.isEmpty()) {
                item {
                    Text(
                        if (onglet == 0) "Aucun objectif cette semaine.\nAppuie sur + pour en créer un, ou duplique un objectif depuis l'Historique."
                        else "Aucun objectif ce mois-ci.\nAppuie sur + pour en créer un, ou duplique un objectif depuis l'Historique.",
                        color = p.texteDoux,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    )
                }
            }
            items(liste, key = { it.id }) { o ->
                if (onglet == 0) CarteHebdo(o, aujourdhui, donnees, onCycler, onMenu)
                else CarteMensuel(o, aujourdhui, donnees, onCycler, onMenu)
            }
            if (liste.isNotEmpty()) {
                item {
                    Text(
                        "Appui long sur un objectif : renommer, dupliquer, supprimer",
                        color = p.texteDoux,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = TextAlign.Center,
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
fun EnTete(sousTitre: String, titre: String) {
    val p = LocalPalette.current
    Column {
        Text(sousTitre, color = p.texteDoux, fontSize = 16.sp)
        Text(titre, color = p.texte, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp)
    }
}

/** Les 7 ronds d'une semaine, lettres au-dessus. */
@Composable
fun RondsSemaine(
    objectif: Objectif,
    aujourdhui: LocalDate,
    donnees: Donnees,
    onCycler: (Objectif, LocalDate) -> Unit,
    taille: androidx.compose.ui.unit.Dp,
) {
    LettresJours()
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth()) {
        Periodes.joursSemaine(objectif.debut).forEach { jour ->
            val m = moment(jour, aujourdhui)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Pastille(
                    periode = Periode.HEBDO,
                    etat = donnees.etat(objectif, jour),
                    moment = m,
                    taille = taille,
                    onClick = if (m == Moment.FUTUR) null else ({ onCycler(objectif, jour) }),
                )
            }
        }
    }
}

@Composable
private fun CarteHebdo(
    objectif: Objectif,
    aujourdhui: LocalDate,
    donnees: Donnees,
    onCycler: (Objectif, LocalDate) -> Unit,
    onMenu: (Objectif) -> Unit,
) {
    val p = LocalPalette.current
    Carte(onAppuiLong = { onMenu(objectif) }) {
        Text(objectif.nom, color = p.texte, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        RondsSemaine(objectif, aujourdhui, donnees, onCycler, 38.dp)
    }
}

/** Carte d'un objectif mensuel : compteur, barre, carré du jour (si mois en cours) et calendrier. */
@Composable
fun CarteMensuel(
    objectif: Objectif,
    aujourdhui: LocalDate,
    donnees: Donnees,
    onCycler: (Objectif, LocalDate) -> Unit,
    onMenu: (Objectif) -> Unit,
    afficherPourcentage: Boolean = false,
) {
    val p = LocalPalette.current
    val mois = objectif.mois
    val total = mois.lengthOfMonth()
    val reussis = donnees.reussis(objectif)
    val moisCourant = mois == YearMonth.from(aujourdhui)
    Carte(onAppuiLong = { onMenu(objectif) }) {
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
                Text(
                    if (afficherPourcentage) "$reussis / $total j · ${donnees.pourcentage(objectif)} %" else "$reussis / $total j",
                    color = p.texte,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
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
