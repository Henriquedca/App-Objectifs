package com.henrique.objectifs.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henrique.objectifs.data.Donnees
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periodes
import com.henrique.objectifs.data.Moment
import com.henrique.objectifs.data.moment
import java.time.LocalDate
import java.time.YearMonth

/** Historique par mois : objectifs du mois (calendrier) + objectifs hebdo de chaque semaine du mois. */
@Composable
fun EcranHistorique(
    donnees: Donnees,
    aujourdhui: LocalDate,
    onCycler: (Objectif, LocalDate) -> Unit,
    onMenu: (Objectif) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    var moisTexte by rememberSaveable { mutableStateOf(YearMonth.from(aujourdhui).toString()) }
    val mois = YearMonth.parse(moisTexte)
    val mensuels = donnees.mensuelsDe(mois)
    val semaines = Periodes.semainesDuMois(mois).reversed()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.Bottom) {
                Box(Modifier.weight(1f)) {
                    EnTete(sousTitre = Periodes.nomMois(mois), titre = "Historique")
                }
                BoutonFleche(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mois précédent") {
                    moisTexte = mois.minusMonths(1).toString()
                }
                Spacer(Modifier.width(10.dp))
                BoutonFleche(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Mois suivant") {
                    moisTexte = mois.plusMonths(1).toString()
                }
            }
        }

        item { Section("Objectifs du mois") }
        if (mensuels.isEmpty()) {
            item { Vide("Aucun objectif mensuel") }
        }
        items(mensuels, key = { it.id }) { o ->
            Column {
                CarteMensuel(o, aujourdhui, donnees, onCycler, onMenu, afficherPourcentage = true)
                Spacer(Modifier.height(10.dp))
                Legende()
            }
        }

        item { Section("Objectifs hebdomadaires") }
        if (semaines.none { donnees.hebdoDe(it).isNotEmpty() }) {
            item { Vide("Aucun objectif hebdomadaire") }
        }
        items(semaines, key = { "s$it" }) { lundi ->
            val hebdo = donnees.hebdoDe(lundi)
            if (hebdo.isNotEmpty()) {
                CarteSemaine(lundi, hebdo, aujourdhui, donnees, onCycler, onMenu)
            }
        }
    }
}

@Composable
private fun Section(titre: String) {
    val p = LocalPalette.current
    Text(titre, color = p.texte, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun Vide(texte: String) {
    val p = LocalPalette.current
    Text(texte, color = p.texteDoux, fontSize = 15.sp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarteSemaine(
    lundi: LocalDate,
    hebdo: List<Objectif>,
    aujourdhui: LocalDate,
    donnees: Donnees,
    onCycler: (Objectif, LocalDate) -> Unit,
    onMenu: (Objectif) -> Unit,
) {
    val p = LocalPalette.current
    val terminee = lundi.plusDays(6).isBefore(aujourdhui)
    Carte {
        Text(
            "Sem. ${Periodes.libelleSemaine(lundi)}",
            color = p.texteDoux,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        hebdo.forEachIndexed { i, o ->
            if (i > 0) HorizontalDivider(color = p.separateur)
            val reussis = donnees.reussis(o)
            val couleurScore = when {
                !terminee -> p.texteDoux
                reussis == 7 -> p.badgeVertTexte
                else -> p.badgeOrangeTexte
            }
            Row(
                Modifier.fillMaxWidth()
                    .combinedClickable(onClick = {}, onLongClick = { onMenu(o) })
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        o.titre,
                        color = p.texte,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "$reussis / 7 · ${donnees.pourcentage(o)} %",
                        color = couleurScore,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Periodes.joursSemaine(o.debut).forEachIndexed { j, jour ->
                        val m = moment(jour, aujourdhui)
                        RondLettre(
                            lettre = Periodes.LETTRES_JOURS[j],
                            etat = donnees.etat(o, jour),
                            moment = m,
                            taille = 27.dp,
                            onClick = if (m == Moment.FUTUR) null else ({ onCycler(o, jour) }),
                        )
                    }
                }
            }
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
