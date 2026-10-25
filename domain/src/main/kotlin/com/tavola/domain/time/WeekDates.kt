package com.tavola.domain.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

const val DAYS_IN_WEEK = 7

/** Primer día de la semana que contiene esta fecha (lunes por defecto). */
fun LocalDate.startOfWeek(firstDay: DayOfWeek = DayOfWeek.MONDAY): LocalDate {
    val daysSinceStart = (dayOfWeek.ordinal - firstDay.ordinal + DAYS_IN_WEEK) % DAYS_IN_WEEK
    return minus(daysSinceStart, DateTimeUnit.DAY)
}

/** Fecha desplazada [weeks] semanas (negativo = hacia atrás). */
fun LocalDate.plusWeeks(weeks: Int): LocalDate = plus(weeks * DAYS_IN_WEEK, DateTimeUnit.DAY)

/** Los siete días consecutivos a partir de esta fecha. */
fun LocalDate.weekDays(): List<LocalDate> = (0 until DAYS_IN_WEEK).map { plus(it, DateTimeUnit.DAY) }
