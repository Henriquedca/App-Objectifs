package com.henrique.objectifs.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import com.henrique.objectifs.data.Etat
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periode

@Composable
fun DialogueAjout(onAjouter: (String, Periode) -> Unit, onAnnuler: () -> Unit) {
    var nom by rememberSaveable { mutableStateOf("") }
    var periode by rememberSaveable { mutableStateOf(Periode.HEBDO) }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Nouvel objectif") },
        text = {
            Column {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Objectif (ex. 0 fast food)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                ChoixPeriode("Hebdomadaire", Periode.HEBDO, periode == Periode.HEBDO) { periode = Periode.HEBDO }
                ChoixPeriode("Mensuel", Periode.MENSUEL, periode == Periode.MENSUEL) { periode = Periode.MENSUEL }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAjouter(nom, periode) }, enabled = nom.isNotBlank()) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } },
    )
}

@Composable
private fun ChoixPeriode(libelle: String, periode: Periode, choisi: Boolean, onChoisir: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().selectable(selected = choisi, onClick = onChoisir),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = choisi, onClick = onChoisir)
        Text(libelle, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Pastille(Etat.VIDE, periode, taille = 18.dp)
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
fun DialogueSuppression(objectif: Objectif, onSupprimer: () -> Unit, onAnnuler: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Supprimer l'objectif ?") },
        text = { Text("« ${objectif.nom} » et tout son historique seront supprimés.") },
        confirmButton = { TextButton(onClick = onSupprimer) { Text("Supprimer") } },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } },
    )
}
