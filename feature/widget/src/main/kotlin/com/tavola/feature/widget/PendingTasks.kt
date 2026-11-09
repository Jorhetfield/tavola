package com.tavola.feature.widget

import com.tavola.domain.model.DayTask

/**
 * The ticks the person has just made on the widget that the calendar has not confirmed yet, written as "taskId|done". The widget
 * shows them at once, over what it last read, and drops them when it has read the day again: a tap must not wait for the server.
 */
internal object PendingTasks {
    private const val SEPARATOR = '|'

    /** [pending] with the new state of [taskId]: a task that was ticked and unticked before the answer arrived keeps only the last one. */
    fun with(pending: Set<String>, taskId: String, done: Boolean): Set<String> =
        pending.filterNot { entry -> entry.substringBeforeLast(SEPARATOR) == taskId }.toSet() + "$taskId$SEPARATOR$done"

    /** [tasks] as the person last left them: each one with the pending state it has, if any. */
    fun apply(pending: Set<String>, tasks: List<DayTask>): List<DayTask> {
        if (pending.isEmpty()) return tasks
        val doneById = pending.associate { entry ->
            entry.substringBeforeLast(SEPARATOR) to entry.substringAfterLast(SEPARATOR).toBoolean()
        }
        return tasks.map { task -> doneById[task.id]?.let { done -> task.copy(done = done) } ?: task }
    }
}

/** This state with the [pending] ticks laid over its tasks. Only the content of a day has tasks to change. */
internal fun TodayWidgetState.withPending(pending: Set<String>): TodayWidgetState = when (this) {
    is TodayWidgetState.Content -> copy(tasks = PendingTasks.apply(pending, tasks))
    else -> this
}
