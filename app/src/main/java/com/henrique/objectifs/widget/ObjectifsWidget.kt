package com.henrique.objectifs.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.henrique.objectifs.MainActivity
import com.henrique.objectifs.R
import com.henrique.objectifs.data.Etat
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periode
import com.henrique.objectifs.data.Periodes
import com.henrique.objectifs.data.Stockage
import java.time.LocalDate
import java.time.YearMonth

/** Glance limite le nombre d'enfants par Row/Column : 4 objectifs max par type, regroupés en sous-colonnes. */
private const val MAX_PAR_TYPE = 4
private val CELLULE = 22.dp
private val VERSION = intPreferencesKey("version")
private val BLANC = ColorProvider(Color.White)
private val BLANC_DOUX = ColorProvider(Color(0xB3FFFFFF))

class ObjectifsWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stockage = Stockage(context)
        provideContent {
            // Lire la version abonne la composition aux rafraîchissements.
            val version = currentState<Preferences>()[VERSION] ?: 0
            Contenu(stockage, LocalDate.now(), version)
        }
    }
}

class ObjectifsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ObjectifsWidget()
}

/** À appeler après toute modification des données (app ou widget). */
suspend fun rafraichirWidgets(context: Context) {
    val ids = GlanceAppWidgetManager(context).getGlanceIds(ObjectifsWidget::class.java)
    ids.forEach { id ->
        updateAppWidgetState(context, id) { prefs -> prefs[VERSION] = (prefs[VERSION] ?: 0) + 1 }
        ObjectifsWidget().update(context, id)
    }
}

private val PARAM_ID = ActionParameters.Key<String>("id")
private val PARAM_CLE = ActionParameters.Key<String>("cle")

class CyclerEtatAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[PARAM_ID] ?: return
        val cle = parameters[PARAM_CLE] ?: return
        Stockage(context).cycler(id, cle)
        rafraichirWidgets(context)
    }
}

@Composable
private fun Contenu(stockage: Stockage, aujourdhui: LocalDate, @Suppress("UNUSED_PARAMETER") version: Int) {
    val lundi = Periodes.lundi(aujourdhui)
    val mois = YearMonth.from(aujourdhui)
    val tous = stockage.objectifs()
    val hebdo = tous.filter { it.visiblePourSemaine(lundi) }.take(MAX_PAR_TYPE)
    val mensuels = tous.filter { it.visiblePourMois(mois) }.take(MAX_PAR_TYPE)
    val cleSemaine = Periodes.cleSemaine(lundi)
    val cleMois = Periodes.cleMois(mois)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(16.dp)
            .background(Color(0x66000000))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        EnTete(aujourdhui)
        if (hebdo.isEmpty() && mensuels.isEmpty()) {
            Text(
                text = "Aucun objectif",
                style = TextStyle(color = BLANC_DOUX, fontSize = 13.sp),
                modifier = GlanceModifier.padding(top = 6.dp).clickable(actionStartActivity<MainActivity>()),
            )
        }
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            hebdo.forEach { o -> Ligne(o, stockage.etat(o.id, cleSemaine), cleSemaine) }
        }
        if (hebdo.isNotEmpty() && mensuels.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(4.dp))
        }
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            mensuels.forEach { o -> Ligne(o, stockage.etat(o.id, cleMois), cleMois) }
        }
    }
}

@Composable
private fun EnTete(aujourdhui: LocalDate) {
    val indexAujourdhui = aujourdhui.dayOfWeek.value - 1
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 2.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = GlanceModifier.defaultWeight())
        Periodes.LETTRES_JOURS.forEachIndexed { i, lettre ->
            val estAujourdhui = i == indexAujourdhui
            Text(
                text = lettre,
                modifier = GlanceModifier.width(CELLULE),
                style = TextStyle(
                    color = if (estAujourdhui) BLANC else BLANC_DOUX,
                    fontSize = 12.sp,
                    fontWeight = if (estAujourdhui) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

@Composable
private fun Ligne(objectif: Objectif, etat: Etat, cle: String) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = objectif.nom,
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight().clickable(actionStartActivity<MainActivity>()),
            style = TextStyle(color = BLANC, fontSize = 13.sp),
        )
        // Cellule alignée sous le « D »
        Box(
            modifier = GlanceModifier.width(CELLULE).height(CELLULE)
                .clickable(actionRunCallback<CyclerEtatAction>(actionParametersOf(PARAM_ID to objectif.id, PARAM_CLE to cle))),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(dessin(objectif.periode, etat)),
                contentDescription = "${objectif.nom} : ${etat.name.lowercase()}",
                modifier = GlanceModifier.size(16.dp),
            )
        }
    }
}

private fun dessin(periode: Periode, etat: Etat): Int = when (periode) {
    Periode.HEBDO -> when (etat) {
        Etat.VIDE -> R.drawable.rond_vide
        Etat.REUSSI -> R.drawable.rond_vert
        Etat.RATE -> R.drawable.rond_rouge
    }
    Periode.MENSUEL -> when (etat) {
        Etat.VIDE -> R.drawable.carre_vide
        Etat.REUSSI -> R.drawable.carre_vert
        Etat.RATE -> R.drawable.carre_rouge
    }
}
