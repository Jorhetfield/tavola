package com.tavola.data.calendar

import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CalendarDocumentsTest {
    private val tuesday = LocalDate(2026, 10, 20)
    private val recipe = PlannedMeal.FromRecipe(RecipeRef("paella", "Paella valenciana", 50, listOf("Español")))

    @Test
    fun `the id of a slot is the day and the meal`() {
        assertEquals("2026-10-20_Lunch", mealDocumentId(tuesday, MealType.Lunch))
        assertEquals("2026-10-20_Dinner", mealDocumentId(tuesday, MealType.Dinner))
    }

    @Test
    fun `a recipe meal is read back with its copy of the title, the time and the tags`() {
        val document = recipe.toDocument(tuesday, MealType.Lunch, createdBy = "user-marta")

        assertEquals(StoredMeal(tuesday, MealType.Lunch, recipe), document.toStoredMeal())
    }

    @Test
    fun `a quick meal is read back with its note`() {
        val quick = PlannedMeal.Quick("Comemos fuera", note = "Con amigos")

        val stored = quick.toDocument(tuesday, MealType.Dinner, createdBy = null).toStoredMeal()

        assertEquals(StoredMeal(tuesday, MealType.Dinner, quick), stored)
    }

    @Test
    fun `a meal document has the shape the rules ask for`() {
        val document = recipe.toDocument(tuesday, MealType.Lunch, createdBy = "user-marta")

        assertEquals(setOf("date", "type", "title", "recipeId", "prepTimeMinutes", "tags", "createdBy"), document.keys)
        assertEquals(
            setOf("date", "type", "title", "tags", "note"),
            PlannedMeal.Quick("Sobras").toDocument(tuesday, MealType.Lunch, null).keys,
        )
    }

    @Test
    fun `a meal document with a bad date, type or title is not a meal`() {
        val valid = recipe.toDocument(tuesday, MealType.Lunch, createdBy = null)

        assertNull((valid + mapOf("date" to "20-10-2026")).toStoredMeal())
        assertNull((valid + mapOf("type" to "Brunch")).toStoredMeal())
        assertNull((valid - "title").toStoredMeal())
    }

    @Test
    fun `numbers from Firestore come as longs and are read as the right type`() {
        val document = recipe.toDocument(tuesday, MealType.Lunch, createdBy = null) + mapOf("prepTimeMinutes" to 50L)

        assertEquals(50, ((document.toStoredMeal()?.meal) as PlannedMeal.FromRecipe).recipe.prepTimeMinutes)
    }

    @Test
    fun `a task is read back as it was written, with its day, creation time and author`() {
        val task = DayTask("t1", "Comprar pan", done = true, createdAt = 1_700_000_000_000L, createdBy = "user-marta")

        val stored = task.toDocument(tuesday, createdBy = "user-marta").toStoredTask("t1")

        assertEquals(StoredTask(tuesday, task), stored)
    }

    @Test
    fun `without an author the document leaves the field out, so a merge keeps who made the task`() {
        val document = DayTask("t1", "Comprar pan", createdAt = 5).toDocument(tuesday, createdBy = null)

        assertFalse(document.containsKey("createdBy"))
        assertEquals(setOf("date", "text", "done", "createdAt"), document.keys)
    }

    @Test
    fun `a task document has the shape the rules ask for`() {
        val document = DayTask("t1", "Comprar pan", createdAt = 5).toDocument(tuesday, createdBy = "user-marta")

        assertEquals(setOf("date", "text", "done", "createdAt", "createdBy"), document.keys)
    }

    @Test
    fun `a task document without a valid day or a text is not a task, and a missing flag reads as pending`() {
        val valid = DayTask("t1", "Comprar pan", createdAt = 5).toDocument(tuesday, createdBy = null)

        assertNull((valid + mapOf("date" to "mañana")).toStoredTask("t1"))
        assertNull((valid - "text").toStoredTask("t1"))
        assertEquals(false, (valid - "done").toStoredTask("t1")?.task?.done)
        assertEquals(0L, (valid - "createdAt").toStoredTask("t1")?.task?.createdAt)
    }

    @Test
    fun `the days of a week are found by a range of date strings`() {
        // The queries compare ISO dates as text, which only works because the text order is the date order.
        val dates = (0..6).map { offset -> LocalDate(2026, 10, 19 + offset).toString() }

        assertEquals(dates.sorted(), dates)
        assertEquals(listOf("2026-10-19", "2026-10-25"), listOf(dates.first(), dates.last()))
    }
}
