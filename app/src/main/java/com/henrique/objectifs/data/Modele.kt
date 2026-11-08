package com.henrique.objectifs.data

import java.time.LocalDate
import java.time.YearMonth

/** HEBDO = ronds (L→D), MENSUEL = carré + compteur de jours. Les deux se suivent jour par jour. */
enum class Periode { HEBDO, MENSUEL }

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

data class Objectif(
    val id: String,
    val nom: String,
    val periode: Periode,
    val creeLe: LocalDate,
)

/** Instantané immuable des données, recréé à chaque modification (déclenche la recomposition). */
data class Donnees(
    val objectifs: List<Objectif>,
    val etats: Map<String, Etat>,
) {
    val hebdo: List<Objectif> get() = objectifs.filter { it.periode == Periode.HEBDO }
    val mensuels: List<Objectif> get() = objectifs.filter { it.periode == Periode.MENSUEL }

    fun etat(objectif: Objectif, jour: LocalDate): Etat = etats["${objectif.id}|$jour"] ?: Etat.VIDE

    fun reussis(objectif: Objectif, jours: List<LocalDate>): Int = jours.count { etat(objectif, it) == Etat.REUSSI }

    fun reussisDuMois(objectif: Objectif, mois: YearMonth): Int =
        reussis(objectif, (1..mois.lengthOfMonth()).map { mois.atDay(it) })
}
