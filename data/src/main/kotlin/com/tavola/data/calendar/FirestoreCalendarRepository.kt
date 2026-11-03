package com.tavola.data.calendar

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.tavola.data.group.GroupPaths
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.WeekPlan
import com.tavola.domain.repository.AuthRepository
import com.tavola.domain.repository.CalendarRepository
import com.tavola.domain.repository.GroupRepository
import com.tavola.domain.time.weekDays
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.LocalDate

/**
 * The calendar of the group in Firestore (see `CalendarDocuments.kt`): every member sees the same meals and tasks, and a change
 * shows up on every phone through the listeners. The group of the person comes from [GroupRepository].
 *
 * A change is shown at once on this phone and is sent when there is a connection, so a write only waits a short while for the
 * server (instead of hanging while offline). If the server refuses it, or the task was deleted by someone else meanwhile, the
 * calendar goes back to what the server has; the callers are fire-and-forget, so a failure is not thrown at them.
 */
@Singleton
internal class FirestoreCalendarRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
) : CalendarRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeWeek(weekStart: LocalDate): Flow<WeekPlan> = groupRepository.groupId.flatMapLatest { groupId ->
        val dates = weekStart.weekDays()
        if (groupId == null) {
            flowOf(WeekPlan(groupId = "", weekStart = weekStart, days = dates.map { date -> DayPlan(date) }))
        } else {
            val first = dates.first().toString()
            val last = dates.last().toString()
            combine(meals(groupId).between(first, last).snapshots(), tasks(groupId).between(first, last).snapshots()) {
                    mealsSnapshot,
                    tasksSnapshot,
                ->
                val storedMeals = mealsSnapshot.documents.mapNotNull { document -> document.data?.toStoredMeal() }
                val storedTasks = tasksSnapshot.documents.mapNotNull { document -> document.data?.toStoredTask(document.id) }
                WeekPlan(
                    groupId = groupId,
                    weekStart = weekStart,
                    days = dates.map { date ->
                        DayPlan(
                            date = date,
                            lunch = storedMeals.mealOf(date, MealType.Lunch),
                            dinner = storedMeals.mealOf(date, MealType.Dinner),
                            tasks = storedTasks
                                .filter { stored -> stored.date == date }
                                .map { stored -> stored.task }
                                .sortedWith(compareBy({ task -> task.createdAt }, { task -> task.id })),
                        )
                    },
                )
            }
        }
    }

    override suspend fun setTaskDone(date: LocalDate, taskId: String, done: Boolean) = write { groupId ->
        tasks(groupId).document(taskId).update(TaskFields.DONE, done).await()
    }

    override suspend fun addTask(date: LocalDate, text: String) = write { groupId ->
        val reference = tasks(groupId).document()
        val task = DayTask(id = reference.id, text = text, createdAt = System.currentTimeMillis())
        reference.set(task.toDocument(date, createdBy = authRepository.currentUser.first()?.id)).await()
    }

    override suspend fun updateTaskText(date: LocalDate, taskId: String, text: String) = write { groupId ->
        tasks(groupId).document(taskId).update(TaskFields.TEXT, text).await()
    }

    override suspend fun removeTask(date: LocalDate, taskId: String) = write { groupId ->
        tasks(groupId).document(taskId).delete().await()
    }

    override suspend fun restoreTask(date: LocalDate, task: DayTask) = write { groupId ->
        // Merging keeps who made the task if it still exists, and brings it back with its creation time (so, in its place) if not.
        tasks(groupId).document(task.id).set(task.toDocument(date, createdBy = task.createdBy), SetOptions.merge()).await()
    }

    override suspend fun setMeal(date: LocalDate, type: MealType, meal: PlannedMeal?) = write { groupId ->
        val reference = meals(groupId).document(mealDocumentId(date, type))
        if (meal == null) {
            reference.delete().await()
        } else {
            reference.set(meal.toDocument(date, type, createdBy = authRepository.currentUser.first()?.id)).await()
        }
    }

    /**
     * Runs a change in the group of the person, waiting a little for the server. With no group there is nothing to change, and a
     * refusal of the server (a task that was deleted meanwhile, say) is not an error for the caller: the listeners show the result.
     */
    private suspend fun write(change: suspend (groupId: String) -> Unit) {
        val groupId = withTimeoutOrNull(GROUP_WAIT_MILLIS) { groupRepository.groupId.filterNotNull().first() } ?: return
        try {
            withTimeoutOrNull(ACKNOWLEDGEMENT_WAIT_MILLIS) { change(groupId) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (refused: FirebaseFirestoreException) {
            // The calendar goes back to what the server has.
        }
    }

    private fun List<StoredMeal>.mealOf(date: LocalDate, type: MealType): PlannedMeal? =
        firstOrNull { stored -> stored.date == date && stored.type == type }?.meal

    private fun CollectionReference.between(first: String, last: String): Query =
        whereGreaterThanOrEqualTo(DATE_FIELD, first).whereLessThanOrEqualTo(DATE_FIELD, last)

    private fun meals(groupId: String): CollectionReference = group(groupId).collection(MEALS_COLLECTION)

    private fun tasks(groupId: String): CollectionReference = group(groupId).collection(TASKS_COLLECTION)

    private fun group(groupId: String) = firestore.collection(GroupPaths.GROUPS).document(groupId)

    private companion object {
        const val ACKNOWLEDGEMENT_WAIT_MILLIS = 4_000L
        const val GROUP_WAIT_MILLIS = 10_000L
    }
}
