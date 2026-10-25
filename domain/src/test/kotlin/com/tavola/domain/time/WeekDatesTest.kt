package com.tavola.domain.time

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WeekDatesTest {
    private val monday = LocalDate(2026, 10, 19)

    @Test
    fun `el lunes es el inicio de su propia semana`() {
        assertEquals(monday, monday.startOfWeek())
    }

    @Test
    fun `cualquier dia de la semana apunta al lunes anterior`() {
        assertEquals(monday, LocalDate(2026, 10, 22).startOfWeek())
        assertEquals(monday, LocalDate(2026, 10, 25).startOfWeek())
    }

    @Test
    fun `la semana puede empezar en otro dia`() {
        assertEquals(LocalDate(2026, 10, 18), LocalDate(2026, 10, 22).startOfWeek(DayOfWeek.SUNDAY))
    }

    @Test
    fun `el inicio de semana cruza correctamente el cambio de mes y de ano`() {
        assertEquals(LocalDate(2026, 12, 28), LocalDate(2027, 1, 2).startOfWeek())
    }

    @Test
    fun `plusWeeks mueve de siete en siete dias en ambos sentidos`() {
        assertEquals(LocalDate(2026, 10, 26), monday.plusWeeks(1))
        assertEquals(LocalDate(2026, 10, 12), monday.plusWeeks(-1))
    }

    @Test
    fun `weekDays devuelve siete dias consecutivos`() {
        val days = monday.weekDays()

        assertEquals(7, days.size)
        assertEquals(monday, days.first())
        assertEquals(LocalDate(2026, 10, 25), days.last())
    }
}
