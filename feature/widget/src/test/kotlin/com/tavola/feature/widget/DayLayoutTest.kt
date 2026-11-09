package com.tavola.feature.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DayLayoutTest {
    @Test
    fun `a narrow widget stacks the slots, a short one makes them compact and a big one shows them whole`() {
        assertEquals(DayLayout.Narrow, dayLayoutFor(DpSize(154.dp, 154.dp)))
        assertEquals(DayLayout.Narrow, dayLayoutFor(DpSize(150.dp, 400.dp)))
        assertEquals(DayLayout.Short, dayLayoutFor(DpSize(310.dp, 130.dp)))
        assertEquals(DayLayout.Full, dayLayoutFor(DpSize(310.dp, 300.dp)))
    }

    @Test
    fun `a day with a couple of short tasks fits in a big widget, so it needs no scroll`() {
        assertTrue(dayFits(DayLayout.Full, listOf("Sacar el pollo", "Comprar pan"), DpSize(310.dp, 420.dp), fontScale = 1f))
    }

    @Test
    fun `a day with many tasks does not fit, so it is a list`() {
        val tasks = List(12) { index -> "Tarea $index" }

        assertFalse(dayFits(DayLayout.Full, tasks, DpSize(310.dp, 300.dp), fontScale = 1f))
    }

    @Test
    fun `a short widget cannot show the tasks too, so it is a list`() {
        assertFalse(dayFits(DayLayout.Short, listOf("Sacar el pollo", "Comprar pan"), DpSize(310.dp, 130.dp), fontScale = 1f))
    }

    @Test
    fun `a long task takes two lines and large text takes more room`() {
        val short = estimatedDayHeight(DayLayout.Full, listOf("Pan"), 310.dp, fontScale = 1f)
        val long =
            estimatedDayHeight(DayLayout.Full, listOf("Preparar la masa de la pizza para la cena de esta noche"), 310.dp, fontScale = 1f)
        val large = estimatedDayHeight(DayLayout.Full, listOf("Pan"), 310.dp, fontScale = 1.5f)

        assertTrue(long > short)
        assertTrue(large > short)
    }

    @Test
    fun `with no tasks there is a line saying so`() {
        assertTrue(estimatedDayHeight(DayLayout.Full, emptyList(), 310.dp, fontScale = 1f) > 0.dp)
    }
}
