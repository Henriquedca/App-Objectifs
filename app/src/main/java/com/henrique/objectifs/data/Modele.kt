package com.henrique.objectifs.data

import java.time.LocalDate
import java.time.YearMonth

/** HEBDO = rond, MENSUEL = carré. */
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

data class Objectif(
    val id: String,
    val nom: String,
    val periode: Periode,
    val creeLe: LocalDate,
) {
    /** Un objectif est affiché à partir du mois de sa création (pour pouvoir remplir les semaines déjà écoulées du mois). */
    fun visiblePourSemaine(lundi: LocalDate): Boolean =
        periode == Periode.HEBDO && !Periodes.moisDeSemaine(lundi).isBefore(YearMonth.from(creeLe))

    fun visiblePourMois(mois: YearMonth): Boolean =
        periode == Periode.MENSUEL && !mois.isBefore(YearMonth.from(creeLe))
}

/** Instantané immuable des données, recréé à chaque modification (déclenche la recomposition). */
data class Donnees(
    val objectifs: List<Objectif>,
    val etats: Map<String, Etat>,
) {
    fun etat(objectif: Objectif, cle: String): Etat = etats["${objectif.id}|$cle"] ?: Etat.VIDE

    fun hebdoPour(lundi: LocalDate): List<Objectif> = objectifs.filter { it.visiblePourSemaine(lundi) }

    fun mensuelsPour(mois: YearMonth): List<Objectif> = objectifs.filter { it.visiblePourMois(mois) }
}
