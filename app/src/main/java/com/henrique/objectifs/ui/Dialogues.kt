package com.henrique.objectifs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henrique.objectifs.data.Etat
import com.henrique.objectifs.data.LIMITE_NOM
import com.henrique.objectifs.data.Moment
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periode
import com.henrique.objectifs.data.Periodes
import com.henrique.objectifs.data.debutPeriode
import java.time.LocalDate

@Composable
private fun ChampNom(nom: String, onNom: (String) -> Unit) {
    OutlinedTextField(
        value = nom,
        onValueChange = { onNom(it.take(LIMITE_NOM)) },
        label = { Text("Nom") },
        supportingText = { Text("${nom.length} / $LIMITE_NOM", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Choix de la semaine / du mois avec des flèches. */
@Composable
private fun ChoixPeriodeDate(periode: Periode, debut: LocalDate, onDebut: (LocalDate) -> Unit) {
    fun decaler(n: Long) = if (periode == Periode.HEBDO) debut.plusWeeks(n) else debut.plusMonths(n)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = { onDebut(decaler(-1)) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Précédent")
        }
        Text(
            Periodes.libellePeriode(periode, debut),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = { onDebut(decaler(1)) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Suivant")
        }
    }
}

@Composable
fun DialogueAjout(
    periodeInitiale: Periode,
    aujourdhui: LocalDate,
    onAjouter: (String, Periode, LocalDate) -> Unit,
    onAnnuler: () -> Unit,
) {
    var nom by rememberSaveable { mutableStateOf("") }
    var periode by rememberSaveable { mutableStateOf(periodeInitiale) }
    var debutTexte by rememberSaveable { mutableStateOf(debutPeriode(periodeInitiale, aujourdhui).toString()) }
    val debut = LocalDate.parse(debutTexte)
    fun choisir(nouvelle: Periode) {
        periode = nouvelle
        debutTexte = debutPeriode(nouvelle, debut).toString()
    }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Nouvel objectif") },
        text = {
            Column {
                ChampNom(nom) { nom = it }
                Spacer(Modifier.height(8.dp))
                ChoixType("Hebdomadaire", Periode.HEBDO, periode == Periode.HEBDO) { choisir(Periode.HEBDO) }
                ChoixType("Mensuel", Periode.MENSUEL, periode == Periode.MENSUEL) { choisir(Periode.MENSUEL) }
                Spacer(Modifier.height(8.dp))
                ChoixPeriodeDate(periode, debut) { debutTexte = debutPeriode(periode, it).toString() }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAjouter(nom, periode, debut) }, enabled = nom.isNotBlank()) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } },
    )
}

@Composable
private fun ChoixType(libelle: String, periode: Periode, choisi: Boolean, onChoisir: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().selectable(selected = choisi, onClick = onChoisir),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = choisi, onClick = onChoisir)
        Text(libelle, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Pastille(periode, Etat.VIDE, Moment.PASSE, taille = 18.dp)
        Spacer(Modifier.width(8.dp))
    }
}

/** Menu ouvert par un appui long sur un objectif. */
@Composable
fun DialogueMenu(
    objectif: Objectif,
    onRenommer: () -> Unit,
    onDupliquer: () -> Unit,
    onSupprimer: () -> Unit,
    onFermer: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text(objectif.nom) },
        text = {
            Column {
                Text(Periodes.libellePeriode(objectif.periode, objectif.debut), color = LocalPalette.current.texteDoux)
                Spacer(Modifier.height(8.dp))
                LigneMenu("Renommer", onRenommer)
                LigneMenu("Dupliquer", onDupliquer)
                LigneMenu("Supprimer", onSupprimer, couleur = LocalPalette.current.rouge)
            }
        },
        confirmButton = { TextButton(onClick = onFermer) { Text("Fermer") } },
    )
}

@Composable
private fun LigneMenu(texte: String, onClick: () -> Unit, couleur: androidx.compose.ui.graphics.Color = LocalPalette.current.texte) {
    Text(
        texte,
        color = couleur,
        fontSize = 17.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
    )
}

@Composable
fun DialogueRenommer(objectif: Objectif, onRenommer: (String) -> Unit, onAnnuler: () -> Unit) {
    var nom by rememberSaveable { mutableStateOf(objectif.nom.take(LIMITE_NOM)) }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Renommer") },
        text = { ChampNom(nom) { nom = it } },
        confirmButton = {
            TextButton(onClick = { onRenommer(nom) }, enabled = nom.isNotBlank()) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } },
    )
}

/** Duplique l'objectif sur une autre semaine / un autre mois (par défaut la période suivante). */
@Composable
fun DialogueDupliquer(objectif: Objectif, aujourdhui: LocalDate, onDupliquer: (LocalDate) -> Unit, onAnnuler: () -> Unit) {
    val actuel = debutPeriode(objectif.periode, aujourdhui)
    val defaut = if (objectif.debut.isBefore(actuel)) actuel
    else if (objectif.periode == Periode.HEBDO) objectif.debut.plusWeeks(1) else objectif.debut.plusMonths(1)
    var debutTexte by rememberSaveable { mutableStateOf(defaut.toString()) }
    val debut = LocalDate.parse(debutTexte)
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Dupliquer « ${objectif.nom} »") },
        text = {
            Column {
                Text("Vers :")
                Spacer(Modifier.height(4.dp))
                ChoixPeriodeDate(objectif.periode, debut) { debutTexte = it.toString() }
            }
        },
        confirmButton = { TextButton(onClick = { onDupliquer(debut) }) { Text("Dupliquer") } },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } },
    )
}

@Composable
fun DialogueSuppression(objectif: Objectif, onSupprimer: () -> Unit, onAnnuler: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Supprimer l'objectif ?") },
        text = { Text("« ${objectif.nom} » (${Periodes.libellePeriode(objectif.periode, objectif.debut)}) et son suivi seront supprimés.") },
        confirmButton = { TextButton(onClick = onSupprimer) { Text("Supprimer") } },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } },
    )
}
