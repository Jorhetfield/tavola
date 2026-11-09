package com.tavola.feature.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.tavola.domain.model.DayTask
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DayLayoutTest {
    private val big = DpSize(310.dp, 420.dp)

    private fun task(id: String, text: String = "Tarea $id", done: Boolean = false) =
        DayTask(id, text, done, createdAt = id.hashCode().toLong())

    @Test
    fun `a narrow widget stacks the slots, a short one makes them compact and a big one shows them whole`() {
        assertEquals(DayLayout.Narrow, dayLayoutFor(DpSize(154.dp, 154.dp)))
        assertEquals(DayLayout.Narrow, dayLayoutFor(DpSize(150.dp, 400.dp)))
        assertEquals(DayLayout.Short, dayLayoutFor(DpSize(310.dp, 130.dp)))
        assertEquals(DayLayout.Full, dayLayoutFor(DpSize(310.dp, 300.dp)))
    }

    @Test
    fun `a big widget shows the first two tasks that are still to do`() {
        val tasks = listOf(task("a"), task("b"), task("c"), task("d"))

        assertEquals(listOf("a", "b"), visibleTasks(DayLayout.Full, tasks, big, fontScale = 1f)?.map { shown -> shown.id })
    }

    @Test
    fun `a task that is done leaves the widget and the next one takes its place`() {
        val tasks = listOf(task("a", done = true), task("b"), task("c"), task("d"))

        assertEquals(listOf("b", "c"), visibleTasks(DayLayout.Full, tasks, big, fontScale = 1f)?.map { shown -> shown.id })
    }

    @Test
    fun `with one task pending only that one is shown, as long as two would fit`() {
        val tasks = listOf(task("a", done = true), task("b"))

        assertEquals(listOf("b"), visibleTasks(DayLayout.Full, tasks, big, fontScale = 1f)?.map { shown -> shown.id })
    }

    @Test
    fun `with nothing pending there is room for tasks but none to show`() {
        assertEquals(emptyList<DayTask>(), visibleTasks(DayLayout.Full, listOf(task("a", done = true)), big, fontScale = 1f))
        assertEquals(emptyList<DayTask>(), visibleTasks(DayLayout.Full, emptyList(), big, fontScale = 1f))
    }

    @Test
    fun `a widget that cannot show two tasks without scrolling shows none, even if only one is pending`() {
        val tasks = listOf(task("a"), task("b"))

        assertNull(visibleTasks(DayLayout.Narrow, tasks, DpSize(154.dp, 154.dp), fontScale = 1f))
        assertNull(visibleTasks(DayLayout.Short, tasks, DpSize(310.dp, 130.dp), fontScale = 1f))
        assertNull(visibleTasks(DayLayout.Full, listOf(task("a")), DpSize(310.dp, 230.dp), fontScale = 1f))
    }

    @Test
    fun `a 4x4 has room for two tasks`() {
        assertNotNull(visibleTasks(DayLayout.Full, listOf(task("a"), task("b")), DpSize(310.dp, 300.dp), fontScale = 1f))
    }

    @Test
    fun `long tasks take two lines each, so they need more room`() {
        val long = "Preparar la masa de la pizza para la cena de esta noche"
        val tasks = listOf(task("a", long), task("b", long))
        val roomForShortOnes = DpSize(310.dp, 295.dp)

        assertNotNull(visibleTasks(DayLayout.Full, listOf(task("a"), task("b")), roomForShortOnes, fontScale = 1f))
        assertNull(visibleTasks(DayLayout.Full, tasks, roomForShortOnes, fontScale = 1f))
    }

    @Test
    fun `large text takes more room, so tasks that fit at normal size may not`() {
        val tasks = listOf(task("a"), task("b"))
        val size = DpSize(310.dp, 320.dp)

        assertNotNull(visibleTasks(DayLayout.Full, tasks, size, fontScale = 1f))
        assertNull(visibleTasks(DayLayout.Full, tasks, size, fontScale = 1.5f))
    }
}
