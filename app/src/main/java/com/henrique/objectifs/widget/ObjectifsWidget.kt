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
import androidx.glance.appwidget.SizeMode
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
import com.henrique.objectifs.data.Moment
import com.henrique.objectifs.data.Objectif
import com.henrique.objectifs.data.Periode
import com.henrique.objectifs.data.Periodes
import com.henrique.objectifs.data.Stockage
import com.henrique.objectifs.data.moment
import java.time.LocalDate
import java.time.YearMonth

/** Glance limite le nombre d'enfants par Row/Column : 4 objectifs max par type, regroupés en sous-colonnes. */
private const val MAX_PAR_TYPE = 4
private val CELLULE = 30.dp
private val PASTILLE = 27.dp
private val VERSION = intPreferencesKey("version")
private val TEXTE = ColorProvider(Color(0xFF1A1A1A))
private val TEXTE_DOUX = ColorProvider(Color(0xFF55585E))
private val LETTRE_VIDE = ColorProvider(Color(0xFF4A4A4A))
private val LETTRE_PLEINE = ColorProvider(Color.White)

class ObjectifsWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Single

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
private val PARAM_JOUR = ActionParameters.Key<String>("jour")

class CyclerEtatAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[PARAM_ID] ?: return
        val jour = parameters[PARAM_JOUR]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return
        if (jour.isAfter(LocalDate.now())) return
        Stockage(context).cycler(id, jour)
        rafraichirWidgets(context)
    }
}

private fun cliqueCycler(objectif: Objectif, jour: LocalDate) =
    GlanceModifier.clickable(
        actionRunCallback<CyclerEtatAction>(actionParametersOf(PARAM_ID to objectif.id, PARAM_JOUR to jour.toString())),
        rippleOverride = R.drawable.w_clic,
    )

private val ouvrirApp = GlanceModifier.clickable(actionStartActivity<MainActivity>(), rippleOverride = R.drawable.w_clic)

@Composable
private fun Contenu(stockage: Stockage, aujourdhui: LocalDate, @Suppress("UNUSED_PARAMETER") version: Int) {
    val lundi = Periodes.lundi(aujourdhui)
    val mois = YearMonth.from(aujourdhui)
    val tous = stockage.objectifs()
    val hebdo = tous.filter { it.periode == Periode.HEBDO && it.debut == lundi }.take(MAX_PAR_TYPE)
    val mensuels = tous.filter { it.periode == Periode.MENSUEL && it.mois == mois }.take(MAX_PAR_TYPE)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(26.dp)
            .background(Color(0xD9F6F6F4))
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(
            text = "\uD83C\uDFAF Objectifs",
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp).then(ouvrirApp),
            style = TextStyle(color = TEXTE, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
        )
        if (hebdo.isEmpty() && mensuels.isEmpty()) {
            Text(
                text = "Aucun objectif cette semaine",
                modifier = GlanceModifier.fillMaxWidth().then(ouvrirApp),
                style = TextStyle(color = TEXTE_DOUX, fontSize = 14.sp, textAlign = TextAlign.Center),
            )
        }
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            hebdo.forEach { o -> LigneHebdo(o, stockage, aujourdhui) }
        }
        if (mensuels.isNotEmpty()) {
            if (hebdo.isNotEmpty()) {
                Box(modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp)) {
                    Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(Color(0x33000000))) {}
                }
            }
            Text(
                text = "OBJECTIF DE ${Periodes.nomMoisSeul(mois).uppercase()}",
                modifier = GlanceModifier.fillMaxWidth(),
                style = TextStyle(color = TEXTE_DOUX, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center),
            )
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                mensuels.forEach { o -> LigneMensuel(o, stockage, aujourdhui) }
            }
        }
    }
}

@Composable
private fun LigneHebdo(objectif: Objectif, stockage: Stockage, aujourdhui: LocalDate) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = objectif.titre,
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight().then(ouvrirApp),
            style = TextStyle(color = TEXTE, fontSize = 16.sp, fontWeight = FontWeight.Medium),
        )
        Periodes.joursSemaine(objectif.debut).forEachIndexed { i, jour ->
            val m = moment(jour, aujourdhui)
            val etat = stockage.etat(objectif.id, jour)
            var modif = GlanceModifier.width(CELLULE).height(CELLULE)
            if (m != Moment.FUTUR) modif = modif.then(cliqueCycler(objectif, jour))
            Box(modifier = modif, contentAlignment = Alignment.Center) {
                Image(
                    provider = ImageProvider(dessinRond(etat, m)),
                    contentDescription = null,
                    modifier = GlanceModifier.size(PASTILLE),
                )
                Text(
                    text = Periodes.LETTRES_JOURS[i],
                    style = TextStyle(
                        color = if (etat == Etat.VIDE) LETTRE_VIDE else LETTRE_PLEINE,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

@Composable
private fun LigneMensuel(objectif: Objectif, stockage: Stockage, aujourdhui: LocalDate) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = objectif.titre,
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight().then(ouvrirApp),
            style = TextStyle(color = TEXTE, fontSize = 16.sp, fontWeight = FontWeight.Medium),
        )
        // Carré = aujourd'hui, aligné sous le « D »
        Box(
            modifier = GlanceModifier.width(CELLULE).height(CELLULE).then(cliqueCycler(objectif, aujourdhui)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(dessinCarre(stockage.etat(objectif.id, aujourdhui))),
                contentDescription = null,
                modifier = GlanceModifier.size(PASTILLE),
            )
        }
    }
}

private fun dessinRond(etat: Etat, moment: Moment): Int {
    val auj = moment == Moment.AUJOURDHUI
    return when (etat) {
        Etat.VIDE -> if (auj) R.drawable.w_rond_auj else R.drawable.w_rond_vide
        Etat.REUSSI -> if (auj) R.drawable.w_rond_vert_auj else R.drawable.w_rond_vert
        Etat.RATE -> if (auj) R.drawable.w_rond_rouge_auj else R.drawable.w_rond_rouge
    }
}

private fun dessinCarre(etat: Etat): Int = when (etat) {
    Etat.VIDE -> R.drawable.w_carre_vide
    Etat.REUSSI -> R.drawable.w_carre_vert
    Etat.RATE -> R.drawable.w_carre_rouge
}
