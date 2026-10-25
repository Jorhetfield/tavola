package com.tavola.feature.calendar

import java.time.format.TextStyle
import java.time.temporal.IsoFields
import java.util.Locale
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaMonth

private const val LAST_DAY_OFFSET = 6
private const val THURSDAY_OFFSET = 3

/** "19 – 25 oct" o, si la semana cruza de mes, "26 oct – 1 nov". */
internal fun weekRangeLabel(weekStart: LocalDate, locale: Locale): String {
    val weekEnd = weekStart.plus(LAST_DAY_OFFSET, DateTimeUnit.DAY)
    val endMonth = weekEnd.month.toJavaMonth().getDisplayName(TextStyle.SHORT, locale)
    return if (weekStart.month == weekEnd.month) {
        "${weekStart.dayOfMonth} – ${weekEnd.dayOfMonth} $endMonth"
    } else {
        val startMonth = weekStart.month.toJavaMonth().getDisplayName(TextStyle.SHORT, locale)
        "${weekStart.dayOfMonth} $startMonth – ${weekEnd.dayOfMonth} $endMonth"
    }
}

/** "Octubre 2026". Una semana que cruza de mes cuenta para el mes de su jueves (criterio ISO). */
internal fun monthYearTitle(weekStart: LocalDate, locale: Locale): String {
    val thursday = weekStart.plus(THURSDAY_OFFSET, DateTimeUnit.DAY)
    return "${thursday.month.toJavaMonth().getDisplayName(TextStyle.FULL, locale).capitalized(locale)} ${thursday.year}"
}

/** Número de semana ISO-8601 (la semana del 19 de octubre de 2026 es la 43). */
internal fun isoWeekNumber(weekStart: LocalDate): Int = weekStart.toJavaLocalDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

internal fun LocalDate.weekdayName(locale: Locale): String =
    dayOfWeek.toJavaDayOfWeek().getDisplayName(TextStyle.FULL, locale).capitalized(locale)

private fun String.capitalized(locale: Locale): String = replaceFirstChar { it.titlecase(locale) }
