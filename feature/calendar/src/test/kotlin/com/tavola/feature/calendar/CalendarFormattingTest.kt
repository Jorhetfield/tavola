package com.tavola.feature.calendar

import java.util.Locale
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CalendarFormattingTest {
    private val english = Locale.ENGLISH

    @Test
    fun `una semana dentro de un mes muestra un solo mes`() {
        assertEquals("19 – 25 Oct", weekRangeLabel(LocalDate(2026, 10, 19), english))
    }

    @Test
    fun `una semana que cruza de mes muestra ambos`() {
        assertEquals("26 Oct – 1 Nov", weekRangeLabel(LocalDate(2026, 10, 26), english))
    }

    @Test
    fun `el titulo usa el mes del jueves de la semana`() {
        assertEquals("October 2026", monthYearTitle(LocalDate(2026, 10, 19), english))
        // Semana 26 oct – 1 nov: su jueves es el 29 de octubre.
        assertEquals("October 2026", monthYearTitle(LocalDate(2026, 10, 26), english))
        // Semana 28 dic – 3 ene: su jueves es el 31 de diciembre.
        assertEquals("December 2026", monthYearTitle(LocalDate(2026, 12, 28), english))
    }

    @Test
    fun `la semana del 19 de octubre de 2026 es la 43`() {
        assertEquals(43, isoWeekNumber(LocalDate(2026, 10, 19)))
    }

    @Test
    fun `el nombre del dia empieza en mayuscula`() {
        assertEquals("Monday", LocalDate(2026, 10, 19).weekdayName(english))
        assertEquals("Lunes", LocalDate(2026, 10, 19).weekdayName(Locale.forLanguageTag("es")))
    }
}
