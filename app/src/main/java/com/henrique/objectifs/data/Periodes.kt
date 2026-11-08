package com.henrique.objectifs.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object Periodes {
    val LETTRES_JOURS = listOf("L", "M", "M", "J", "V", "S", "D")

    fun lundi(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun joursSemaine(lundi: LocalDate): List<LocalDate> = (0L..6L).map { lundi.plusDays(it) }

    /** Lignes du calendrier : lundis des semaines qui touchent ce mois. */
    fun lignesCalendrier(mois: YearMonth): List<LocalDate> {
        val fin = lundi(mois.atEndOfMonth())
        return generateSequence(lundi(mois.atDay(1))) { it.plusWeeks(1) }.takeWhile { !it.isAfter(fin) }.toList()
    }

    /** Mois auquel appartient une semaine (celui de son jeudi) : chaque semaine est dans un seul mois. */
    fun moisDeSemaine(lundi: LocalDate): YearMonth = YearMonth.from(lundi.plusDays(3))

    /** Lundis des semaines rattachées à ce mois. */
    fun semainesDuMois(mois: YearMonth): List<LocalDate> =
        lignesCalendrier(mois).filter { moisDeSemaine(it) == mois }

    fun nomMois(mois: YearMonth): String = nomMoisSeul(mois).replaceFirstChar { it.uppercase() } + " " + mois.year

    fun nomMoisSeul(mois: YearMonth): String = mois.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRENCH)

    private fun moisCourt(date: LocalDate): String = date.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH)

    private fun numero(date: LocalDate): String = if (date.dayOfMonth == 1) "1er" else date.dayOfMonth.toString()

    /** « du 2 au 8 nov. » ou « du 26 oct. au 1er nov. » */
    fun libelleSemaine(lundi: LocalDate): String {
        val dimanche = lundi.plusDays(6)
        return if (lundi.month == dimanche.month) {
            "du ${numero(lundi)} au ${numero(dimanche)} ${moisCourt(dimanche)}"
        } else {
            "du ${numero(lundi)} ${moisCourt(lundi)} au ${numero(dimanche)} ${moisCourt(dimanche)}"
        }
    }

    /** « Semaine du 2 au 8 nov. » ou « Novembre 2026 » */
    fun libellePeriode(periode: Periode, debut: LocalDate): String =
        if (periode == Periode.HEBDO) "Semaine ${libelleSemaine(debut)}" else nomMois(YearMonth.from(debut))
}
