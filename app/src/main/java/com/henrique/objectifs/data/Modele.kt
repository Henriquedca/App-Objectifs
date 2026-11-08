package com.henrique.objectifs.data

import java.time.LocalDate
import java.time.YearMonth

/** HEBDO = ronds L→D sur une semaine, MENSUEL = carré + compteur sur un mois. Suivi jour par jour. */
enum class Periode { HEBDO, MENSUEL }

const val LIMITE_NOM = 10

/** 1 clic = REUSSI (vert), 2e clic = RATE (rouge), 3e clic = retour à VIDE. */
enum class Etat {
    VIDE, REUSSI, RATE;

    fun suivant(): Etat = when (this) {
        VIDE -> REUSSI
        REUSSI -> RATE
        RATE -> VIDE
    }
}

enum class Moment { PASSE, AUJOURDHUI, FUTUR }

fun moment(jour: LocalDate, aujourdhui: LocalDate): Moment = when {
    jour.isBefore(aujourdhui) -> Moment.PASSE
    jour == aujourdhui -> Moment.AUJOURDHUI
    else -> Moment.FUTUR
}

/** Début de la période contenant [date] : lundi (hebdo) ou 1er du mois (mensuel). */
fun debutPeriode(periode: Periode, date: LocalDate): LocalDate =
    if (periode == Periode.HEBDO) Periodes.lundi(date) else date.withDayOfMonth(1)

/**
 * Un objectif vit sur UNE période : la semaine qui commence le lundi [debut] (hebdo)
 * ou le mois de [debut] (mensuel). Pour le reconduire, on le duplique.
 */
data class Objectif(
    val id: String,
    val nom: String,
    val periode: Periode,
    val debut: LocalDate,
) {
    val mois: YearMonth get() = YearMonth.from(debut)

    fun jours(): List<LocalDate> =
        if (periode == Periode.HEBDO) Periodes.joursSemaine(debut)
        else (1..mois.lengthOfMonth()).map { mois.atDay(it) }
}

/** Instantané immuable des données, recréé à chaque modification (déclenche la recomposition). */
data class Donnees(
    val objectifs: List<Objectif>,
    val etats: Map<String, Etat>,
) {
    fun hebdoDe(lundi: LocalDate): List<Objectif> =
        objectifs.filter { it.periode == Periode.HEBDO && it.debut == lundi }

    fun mensuelsDe(mois: YearMonth): List<Objectif> =
        objectifs.filter { it.periode == Periode.MENSUEL && it.mois == mois }

    fun etat(objectif: Objectif, jour: LocalDate): Etat = etats["${objectif.id}|$jour"] ?: Etat.VIDE

    fun reussis(objectif: Objectif): Int = objectif.jours().count { etat(objectif, it) == Etat.REUSSI }

    fun pourcentage(objectif: Objectif): Int = Math.round(reussis(objectif) * 100f / objectif.jours().size)
}
