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
    var aSupprimer by remember { mutableStateOf<Objectif?>(null) }

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
                onSupprimer = { aSupprimer = it },
                modifier = Modifier.padding(padding),
            )
        } else {
            EcranHistorique(
                donnees = donnees,
                aujourdhui = aujourdhui,
                onCycler = cycler,
                modifier = Modifier.padding(padding),
            )
        }
    }

    ajout?.let { periodeInitiale ->
        DialogueAjout(
            periodeInitiale = periodeInitiale,
            onAjouter = { nom, periode ->
                stockage.ajouter(nom, periode)
                ajout = null
                modifie()
            },
            onAnnuler = { ajout = null },
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
