package com.henrique.objectifs.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
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
private val PADDING_H = 16.dp
private val VERSION = intPreferencesKey("version")
private val BLANC = ColorProvider(Color.White)
private val GRIS = ColorProvider(Color(0xFFB4B8BF))
private val VERT = ColorProvider(Color(0xFF4ADE80))
private val PISTE = ColorProvider(Color(0x40FFFFFF))

class ObjectifsWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stockage = Stockage(context)
        provideContent {
            // Lire la version abonne la composition aux rafraîchissements.
            val version = currentState<Preferences>()[VERSION] ?: 0
            Contenu(stockage, LocalDate.now(), LocalSize.current.width, version)
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

private fun actionCycler(objectif: Objectif, jour: LocalDate) =
    actionRunCallback<CyclerEtatAction>(actionParametersOf(PARAM_ID to objectif.id, PARAM_JOUR to jour.toString()))

@Composable
private fun Contenu(stockage: Stockage, aujourdhui: LocalDate, largeur: Dp, @Suppress("UNUSED_PARAMETER") version: Int) {
    val tous = stockage.objectifs()
    val hebdo = tous.filter { it.periode == Periode.HEBDO }.take(MAX_PAR_TYPE)
    val mensuels = tous.filter { it.periode == Periode.MENSUEL }.take(MAX_PAR_TYPE)
    // 7 colonnes égales sur toute la largeur : le carré mensuel tombe exactement sous le « D ».
    val cellule = (largeur - PADDING_H * 2) / 7

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(24.dp)
            .background(Color(0xCC2A2D34))
            .padding(horizontal = PADDING_H, vertical = 12.dp)
    ) {
        if (hebdo.isEmpty() && mensuels.isEmpty()) {
            Text(
                text = "Aucun objectif",
                style = TextStyle(color = GRIS, fontSize = 14.sp),
                modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>(), rippleOverride = R.drawable.w_clic),
            )
        }
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            hebdo.forEachIndexed { i, o ->
                BlocHebdo(o, stockage, aujourdhui, cellule, afficherLettres = i == 0, espaceAvant = i > 0)
            }
        }
        if (hebdo.isNotEmpty() && mensuels.isNotEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 10.dp)
            ) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(Color(0x33FFFFFF))) {}
            }
        }
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            mensuels.forEachIndexed { i, o ->
                BlocMensuel(o, stockage, aujourdhui, cellule, espaceAvant = i > 0)
            }
        }
    }
}

@Composable
private fun BlocHebdo(
    objectif: Objectif,
    stockage: Stockage,
    aujourdhui: LocalDate,
    cellule: Dp,
    afficherLettres: Boolean,
    espaceAvant: Boolean,
) {
    val jours = Periodes.joursSemaine(Periodes.lundi(aujourdhui))
    val taillePastille = minOf(cellule - 8.dp, 26.dp)
    Column(modifier = GlanceModifier.fillMaxWidth().padding(top = if (espaceAvant) 8.dp else 0.dp)) {
        Text(
            text = objectif.nom,
            maxLines = 1,
            modifier = GlanceModifier.padding(bottom = 4.dp).clickable(actionStartActivity<MainActivity>(), rippleOverride = R.drawable.w_clic),
            style = TextStyle(color = BLANC, fontSize = 15.sp, fontWeight = FontWeight.Bold),
        )
        if (afficherLettres) {
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Periodes.LETTRES_JOURS.forEach { lettre ->
                    Text(
                        text = lettre,
                        modifier = GlanceModifier.width(cellule),
                        style = TextStyle(color = GRIS, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center),
                    )
                }
            }
        }
        Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 2.dp)) {
            jours.forEach { jour ->
                val m = moment(jour, aujourdhui)
                var modif = GlanceModifier.width(cellule).height(taillePastille + 6.dp)
                if (m != Moment.FUTUR) modif = modif.clickable(actionCycler(objectif, jour), rippleOverride = R.drawable.w_clic)
                Box(modifier = modif, contentAlignment = Alignment.Center) {
                    Image(
                        provider = ImageProvider(dessinRond(stockage.etat(objectif.id, jour), m)),
                        contentDescription = null,
                        modifier = GlanceModifier.size(taillePastille),
                    )
                }
            }
        }
    }
}

@Composable
private fun BlocMensuel(
    objectif: Objectif,
    stockage: Stockage,
    aujourdhui: LocalDate,
    cellule: Dp,
    espaceAvant: Boolean,
) {
    val mois = YearMonth.from(aujourdhui)
    val total = mois.lengthOfMonth()
    val reussis = (1..total).count { stockage.etat(objectif.id, mois.atDay(it)) == Etat.REUSSI }
    val taillePastille = minOf(cellule - 6.dp, 28.dp)
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(top = if (espaceAvant) 8.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight().clickable(actionStartActivity<MainActivity>(), rippleOverride = R.drawable.w_clic)) {
            Text(
                text = "OBJECTIF DE ${Periodes.nomMoisSeul(mois).uppercase()}",
                maxLines = 1,
                style = TextStyle(color = GRIS, fontSize = 10.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                text = objectif.nom,
                maxLines = 1,
                style = TextStyle(color = BLANC, fontSize = 15.sp, fontWeight = FontWeight.Bold),
            )
        }
        Column(
            modifier = GlanceModifier.padding(end = 8.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = "$reussis / $total j",
                style = TextStyle(color = BLANC, fontSize = 13.sp, fontWeight = FontWeight.Bold),
            )
            LinearProgressIndicator(
                progress = reussis.toFloat() / total,
                modifier = GlanceModifier.width(64.dp).height(4.dp).padding(top = 2.dp),
                color = VERT,
                backgroundColor = PISTE,
            )
        }
        // Carré = aujourd'hui, aligné sous le « D »
        Box(
            modifier = GlanceModifier.width(cellule).height(taillePastille + 6.dp)
                .clickable(actionCycler(objectif, aujourdhui), rippleOverride = R.drawable.w_clic),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(dessinCarre(stockage.etat(objectif.id, aujourdhui))),
                contentDescription = null,
                modifier = GlanceModifier.size(taillePastille),
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
    Etat.VIDE -> R.drawable.w_carre_auj
    Etat.REUSSI -> R.drawable.w_carre_vert
    Etat.RATE -> R.drawable.w_carre_rouge
}
