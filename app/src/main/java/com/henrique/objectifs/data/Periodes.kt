package com.henrique.objectifs.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Une semaine va du lundi au dimanche et appartient au mois de son dimanche
 * (la pastille hebdo est placée sous le « D »).
 */
object Periodes {
    val LETTRES_JOURS = listOf("L", "M", "M", "J", "V", "S", "D")

    fun lundi(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun moisDeSemaine(lundi: LocalDate): YearMonth = YearMonth.from(lundi.plusDays(6))

    fun cleSemaine(lundi: LocalDate): String = "S$lundi"

    fun cleMois(mois: YearMonth): String = "M$mois"

    /** Lundis des semaines dont le dimanche tombe dans ce mois. */
    fun semainesDuMois(mois: YearMonth): List<LocalDate> {
        val premierDimanche = mois.atDay(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return generateSequence(premierDimanche) { it.plusWeeks(1) }
            .takeWhile { YearMonth.from(it) == mois }
            .map { it.minusDays(6) }
            .toList()
    }

    fun nomMois(mois: YearMonth): String =
        mois.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRENCH)
            .replaceFirstChar { it.uppercase() } + " " + mois.year

    fun moisCourt(date: LocalDate): String =
        date.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH)

    /** « du 3 au 9 nov. » ou « du 27 oct. au 2 nov. » */
    fun libelleSemaine(lundi: LocalDate): String {
        val dimanche = lundi.plusDays(6)
        return if (lundi.month == dimanche.month) {
            "du ${lundi.dayOfMonth} au ${dimanche.dayOfMonth} ${moisCourt(dimanche)}"
        } else {
            "du ${lundi.dayOfMonth} ${moisCourt(lundi)} au ${dimanche.dayOfMonth} ${moisCourt(dimanche)}"
        }
    }
}
