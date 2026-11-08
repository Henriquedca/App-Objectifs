package com.henrique.objectifs.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

/**
 * Stockage local (SharedPreferences).
 *  - "liste" : JSON des objectifs
 *  - "e|<id>|<aaaa-mm-jj>" : état d'un objectif pour un jour (absent = VIDE)
 */
class Stockage(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(NOM_FICHIER, Context.MODE_PRIVATE)

    fun objectifs(): List<Objectif> {
        val brut = prefs.getString(CLE_LISTE, null) ?: return emptyList()
        return try {
            val tableau = JSONArray(brut)
            (0 until tableau.length()).mapNotNull { i ->
                val o = tableau.optJSONObject(i) ?: return@mapNotNull null
                runCatching {
                    val periode = Periode.valueOf(o.getString("periode"))
                    // Anciennes versions : pas de "debut", on prend la période de création.
                    val date = LocalDate.parse(o.optString("debut").ifEmpty { o.getString("creeLe") })
                    Objectif(
                        id = o.getString("id"),
                        nom = o.getString("nom"),
                        periode = periode,
                        debut = debutPeriode(periode, date),
                    )
                }.getOrNull()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun donnees(): Donnees {
        val etats = HashMap<String, Etat>()
        for ((cle, valeur) in prefs.all) {
            if (!cle.startsWith(PREFIXE_ETAT) || valeur !is String) continue
            val etat = runCatching { Etat.valueOf(valeur) }.getOrNull() ?: continue
            etats[cle.removePrefix(PREFIXE_ETAT)] = etat
        }
        return Donnees(objectifs(), etats)
    }

    fun ajouter(nom: String, periode: Periode, date: LocalDate) {
        val nouveau = Objectif(UUID.randomUUID().toString(), nettoyer(nom), periode, debutPeriode(periode, date))
        enregistrer(objectifs() + nouveau)
    }

    fun renommer(id: String, nom: String) {
        enregistrer(objectifs().map { if (it.id == id) it.copy(nom = nettoyer(nom)) else it })
    }

    /** Copie l'objectif (nom + type) sur la période contenant [date], sans son historique. */
    fun dupliquer(id: String, date: LocalDate) {
        val source = objectifs().firstOrNull { it.id == id } ?: return
        ajouter(source.nom, source.periode, date)
    }

    fun supprimer(id: String) {
        val edition = prefs.edit()
        prefs.all.keys.filter { it.startsWith("$PREFIXE_ETAT$id|") }.forEach { edition.remove(it) }
        edition.putString(CLE_LISTE, versJson(objectifs().filter { it.id != id }))
        edition.commit()
    }

    fun etat(id: String, jour: LocalDate): Etat =
        prefs.getString(cleEtat(id, jour), null)
            ?.let { runCatching { Etat.valueOf(it) }.getOrNull() }
            ?: Etat.VIDE

    fun cycler(id: String, jour: LocalDate): Etat {
        val suivant = etat(id, jour).suivant()
        val edition = prefs.edit()
        if (suivant == Etat.VIDE) edition.remove(cleEtat(id, jour)) else edition.putString(cleEtat(id, jour), suivant.name)
        edition.commit()
        return suivant
    }

    private fun nettoyer(nom: String) = nom.trim().take(LIMITE_NOM)

    private fun enregistrer(liste: List<Objectif>) {
        prefs.edit().putString(CLE_LISTE, versJson(liste)).commit()
    }

    private fun cleEtat(id: String, jour: LocalDate) = "$PREFIXE_ETAT$id|$jour"

    private fun versJson(liste: List<Objectif>): String {
        val tableau = JSONArray()
        liste.forEach { o ->
            tableau.put(
                JSONObject()
                    .put("id", o.id)
                    .put("nom", o.nom)
                    .put("periode", o.periode.name)
                    .put("debut", o.debut.toString())
            )
        }
        return tableau.toString()
    }

    private companion object {
        const val NOM_FICHIER = "objectifs"
        const val CLE_LISTE = "liste"
        const val PREFIXE_ETAT = "e|"
    }
}
