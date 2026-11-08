package com.henrique.objectifs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periodes
import com.henrique.objectifs.data.Stockage
import com.henrique.objectifs.widget.rafraichirWidgets
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun App() {
    val context = LocalContext.current
    val stockage = remember { Stockage(context) }
    val scope = rememberCoroutineScope()

    // Incrémenté à chaque modification et au retour dans l'app (clics faits depuis le widget).
    var version by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        version++
        onPauseOrDispose { }
    }
    val donnees = remember(version) { stockage.donnees() }
    val aujourdhui = remember(version) { LocalDate.now() }

    var moisTexte by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val mois = YearMonth.parse(moisTexte)
    var onglet by rememberSaveable { mutableIntStateOf(0) }
    var ajoutOuvert by rememberSaveable { mutableStateOf(false) }
    var aSupprimer by remember { mutableStateOf<Objectif?>(null) }

    fun modifie() {
        version++
        scope.launch { rafraichirWidgets(context) }
    }

    Scaffold(
        topBar = {
            Column(Modifier.statusBarsPadding()) {
                NavigationMois(
                    mois = mois,
                    onPrecedent = { moisTexte = mois.minusMonths(1).toString() },
                    onSuivant = { moisTexte = mois.plusMonths(1).toString() },
                    onAujourdhui = { moisTexte = YearMonth.now().toString() },
                )
                TabRow(selectedTabIndex = onglet) {
                    Tab(selected = onglet == 0, onClick = { onglet = 0 }, text = { Text("Accueil") })
                    Tab(selected = onglet == 1, onClick = { onglet = 1 }, text = { Text("Historique") })
                }
            }
        },
        floatingActionButton = {
            if (onglet == 0) {
                FloatingActionButton(onClick = { ajoutOuvert = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter un objectif")
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        if (onglet == 0) {
            EcranAccueil(
                donnees = donnees,
                mois = mois,
                aujourdhui = aujourdhui,
                onCycler = { objectif, cle ->
                    stockage.cycler(objectif.id, cle)
                    modifie()
                },
                onAppuiLong = { aSupprimer = it },
                modifier = modifier,
            )
        } else {
            EcranHistorique(donnees = donnees, mois = mois, aujourdhui = aujourdhui, modifier = modifier)
        }
    }

    if (ajoutOuvert) {
        DialogueAjout(
            onAjouter = { nom, periode ->
                stockage.ajouter(nom, periode)
                ajoutOuvert = false
                modifie()
            },
            onAnnuler = { ajoutOuvert = false },
        )
    }
    aSupprimer?.let { objectif ->
        DialogueSuppression(
            objectif = objectif,
            onSupprimer = {
                stockage.supprimer(objectif.id)
                aSupprimer = null
                modifie()
            },
            onAnnuler = { aSupprimer = null },
        )
    }
}

@Composable
private fun NavigationMois(mois: YearMonth, onPrecedent: () -> Unit, onSuivant: () -> Unit, onAujourdhui: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrecedent) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mois précédent")
        }
        Text(
            text = Periodes.nomMois(mois),
            modifier = Modifier.weight(1f).clickable(onClick = onAujourdhui),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = onSuivant) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mois suivant")
        }
    }
}
