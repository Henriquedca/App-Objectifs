package com.henrique.objectifs.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periode
import com.henrique.objectifs.data.Stockage
import com.henrique.objectifs.widget.rafraichirWidgets
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun App() {
    val context = LocalContext.current
    val p = LocalPalette.current
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

    var ecran by rememberSaveable { mutableIntStateOf(0) }
    var ajout by rememberSaveable { mutableStateOf<Periode?>(null) }
    // Dialogues ouverts depuis l'appui long : 0 = menu, 1 = renommer, 2 = dupliquer, 3 = supprimer
    var cible by remember { mutableStateOf<Objectif?>(null) }
    var action by remember { mutableIntStateOf(0) }
    val ouvrirMenu: (Objectif) -> Unit = { cible = it; action = 0 }

    fun modifie() {
        version++
        scope.launch { rafraichirWidgets(context) }
    }

    val cycler: (Objectif, LocalDate) -> Unit = { o, jour ->
        if (!jour.isAfter(LocalDate.now())) {
            stockage.cycler(o.id, jour)
            modifie()
        }
    }

    Scaffold(
        containerColor = p.fond,
        bottomBar = {
            NavigationBar(containerColor = p.carte, tonalElevation = 0.dp) {
                val couleurs = NavigationBarItemDefaults.colors(
                    selectedIconColor = p.vert,
                    selectedTextColor = p.vert,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = p.texteDoux,
                    unselectedTextColor = p.texteDoux,
                )
                NavigationBarItem(
                    selected = ecran == 0,
                    onClick = { ecran = 0 },
                    icon = { Icon(IconeCible, contentDescription = null) },
                    label = { Text("Objectifs", fontWeight = FontWeight.SemiBold) },
                    colors = couleurs,
                )
                NavigationBarItem(
                    selected = ecran == 1,
                    onClick = { ecran = 1 },
                    icon = { Icon(IconeHistogramme, contentDescription = null) },
                    label = { Text("Historique", fontWeight = FontWeight.SemiBold) },
                    colors = couleurs,
                )
            }
        },
    ) { padding ->
        if (ecran == 0) {
            EcranObjectifs(
                donnees = donnees,
                aujourdhui = aujourdhui,
                onCycler = cycler,
                onAjouter = { ajout = it },
                onMenu = ouvrirMenu,
                modifier = Modifier.padding(padding),
            )
        } else {
            EcranHistorique(
                donnees = donnees,
                aujourdhui = aujourdhui,
                onCycler = cycler,
                onMenu = ouvrirMenu,
                modifier = Modifier.padding(padding),
            )
        }
    }

    ajout?.let { periodeInitiale ->
        DialogueAjout(
            periodeInitiale = periodeInitiale,
            aujourdhui = aujourdhui,
            onAjouter = { nom, periode, date ->
                stockage.ajouter(nom, periode, date)
                ajout = null
                modifie()
            },
            onAnnuler = { ajout = null },
        )
    }
    cible?.let { objectif ->
        val fermer = { cible = null }
        when (action) {
            0 -> DialogueMenu(
                objectif = objectif,
                onRenommer = { action = 1 },
                onDupliquer = { action = 2 },
                onSupprimer = { action = 3 },
                onFermer = fermer,
            )
            1 -> DialogueRenommer(
                objectif = objectif,
                onRenommer = { nom ->
                    stockage.renommer(objectif.id, nom)
                    fermer()
                    modifie()
                },
                onAnnuler = fermer,
            )
            2 -> DialogueDupliquer(
                objectif = objectif,
                aujourdhui = aujourdhui,
                onDupliquer = { date ->
                    stockage.dupliquer(objectif.id, date)
                    fermer()
                    modifie()
                },
                onAnnuler = fermer,
            )
            else -> DialogueSuppression(
                objectif = objectif,
                onSupprimer = {
                    stockage.supprimer(objectif.id)
                    fermer()
                    modifie()
                },
                onAnnuler = fermer,
            )
        }
    }
}
