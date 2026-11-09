package com.tavola.feature.widget

import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaMonth

/** "mar 10 nov" or "Tue 10 Nov": the short weekday, the day and the short month, in the language of [locale]. */
internal fun formatWidgetDate(date: LocalDate, locale: Locale): String {
    val weekday = date.dayOfWeek.toJavaDayOfWeek().getDisplayName(JavaTextStyle.SHORT, locale)
    val month = date.month.toJavaMonth().getDisplayName(JavaTextStyle.SHORT, locale)
    return "$weekday ${date.dayOfMonth} $month"
}
