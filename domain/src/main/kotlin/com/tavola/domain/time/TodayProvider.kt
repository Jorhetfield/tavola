package com.tavola.domain.time

import kotlinx.datetime.LocalDate

/** Fecha de hoy. Se inyecta para poder fijarla en los tests (y no depender del reloj del dispositivo). */
fun interface TodayProvider {
    fun today(): LocalDate
}
