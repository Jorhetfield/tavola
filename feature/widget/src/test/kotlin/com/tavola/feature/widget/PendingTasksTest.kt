package com.tavola.feature.widget

import com.tavola.domain.model.DayTask
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PendingTasksTest {
    private val tasks = listOf(DayTask("a", "Sacar el pollo", createdAt = 1), DayTask("b", "Comprar pan", done = true, createdAt = 2))

    @Test
    fun `a pending tick is laid over the task it is for and only that one`() {
        val shown = PendingTasks.apply(PendingTasks.with(emptySet(), "a", done = true), tasks)

        assertEquals(listOf(true, true), shown.map { task -> task.done })
    }

    @Test
    fun `a pending untick turns a done task back to pending`() {
        val shown = PendingTasks.apply(PendingTasks.with(emptySet(), "b", done = false), tasks)

        assertEquals(listOf(false, false), shown.map { task -> task.done })
    }

    @Test
    fun `ticking and unticking before the answer arrives keeps only the last one`() {
        val pending = PendingTasks.with(PendingTasks.with(emptySet(), "a", done = true), "a", done = false)

        assertEquals(1, pending.size)
        assertEquals(listOf(false, true), PendingTasks.apply(pending, tasks).map { task -> task.done })
    }

    @Test
    fun `a task whose id contains the separator is still matched`() {
        val odd = listOf(DayTask("x|y", "Rara", createdAt = 1))

        assertEquals(true, PendingTasks.apply(PendingTasks.with(emptySet(), "x|y", done = true), odd).single().done)
    }

    @Test
    fun `with nothing pending the tasks are the same`() {
        assertEquals(tasks, PendingTasks.apply(emptySet(), tasks))
    }

    @Test
    fun `only the content of a day has tasks to change`() {
        val content = TodayWidgetState.Content(LocalDate(2026, 10, 21), lunch = null, dinner = null, tasks = tasks)

        assertEquals(TodayWidgetState.SignedOut, TodayWidgetState.SignedOut.withPending(setOf("a|true")))
        assertEquals(true, (content.withPending(setOf("a|true")) as TodayWidgetState.Content).tasks.first().done)
    }
}
