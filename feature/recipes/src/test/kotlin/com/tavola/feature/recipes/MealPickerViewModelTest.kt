package com.tavola.feature.recipes

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.core.testing.fake.FakeCalendarRepository
import com.tavola.core.testing.fake.FakeRecipeRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class MealPickerViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val date = LocalDate(2026, 10, 22)
    private val calendarRepository = FakeCalendarRepository(initialDays = listOf(DayPlan(date)))

    private fun viewModel(recipes: RecipeRepository = FakeRecipeRepository()) = MealPickerViewModel(recipes, calendarRepository)

    private suspend fun mealAt(type: MealType): PlannedMeal? {
        var meal: PlannedMeal? = null
        calendarRepository.observeWeek(date).test {
            meal = awaitItem().days.first { it.date == date }.meal(type)
            cancelAndIgnoreRemainingEvents()
        }
        return meal
    }

    @Test
    fun `sin busqueda muestra todas las recetas por orden alfabetico`() = runTest {
        viewModel().uiState.test {
            val state = awaitSuccess()

            assertEquals(MediterraneanSampleData.allRecipes.size, state.recipes.size)
            assertEquals(MediterraneanSampleData.allRecipes.size, state.totalRecipes)
            assertEquals(state.recipes.map { it.title }.sortedBy { it.normalizedForSearch() }, state.recipes.map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `buscar filtra las recetas y conserva el total`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onQueryChange("tiramisu")

            val state = awaitSuccess { it.query == "tiramisu" }
            assertEquals(listOf("Tiramisú"), state.recipes.map { it.title })
            assertEquals(MediterraneanSampleData.allRecipes.size, state.totalRecipes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `una busqueda sin resultados deja la lista vacia`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onQueryChange("sushi")

            assertTrue(awaitSuccess { it.query == "sushi" }.recipes.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `elegir una receta la guarda en el hueco y avisa`() = runTest {
        var done = false

        viewModel().onRecipeSelected(date, MealType.Dinner, MediterraneanSampleData.risotto) { done = true }

        assertEquals(PlannedMeal.FromRecipe(MediterraneanSampleData.risotto.toRef()), mealAt(MealType.Dinner))
        assertTrue(done)
    }

    @Test
    fun `una comida rapida se guarda recortada y con la nota opcional`() = runTest {
        var done = false

        viewModel().onQuickMealConfirmed(date, MealType.Lunch, "  Sobras de ayer ", "   ") { done = true }

        assertEquals(PlannedMeal.Quick("Sobras de ayer", note = null), mealAt(MealType.Lunch))
        assertTrue(done)
    }

    @Test
    fun `una comida rapida con nota la conserva`() = runTest {
        viewModel().onQuickMealConfirmed(date, MealType.Lunch, "Bocadillo", " Para llevar ") {}

        assertEquals(PlannedMeal.Quick("Bocadillo", note = "Para llevar"), mealAt(MealType.Lunch))
    }

    @Test
    fun `una comida rapida sin titulo se ignora`() = runTest {
        var done = false

        viewModel().onQuickMealConfirmed(date, MealType.Lunch, "   ", "nota") { done = true }

        assertNull(mealAt(MealType.Lunch))
        assertEquals(false, done)
    }

    @Test
    fun `quitar vacia el hueco`() = runTest {
        val viewModel = viewModel()
        viewModel.onRecipeSelected(date, MealType.Lunch, MediterraneanSampleData.paella) {}

        viewModel.onClearMeal(date, MealType.Lunch) {}

        assertNull(mealAt(MealType.Lunch))
    }

    @Test
    fun `si el recetario falla muestra error y reintentar lo recupera`() = runTest {
        val flaky = FlakyRecipeRepository()
        val viewModel = viewModel(flaky)
        viewModel.uiState.test {
            while (awaitItem() != MealPickerUiState.Error) continue

            flaky.shouldFail = false
            viewModel.onRetryClick()

            assertTrue(awaitSuccess().recipes.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FlakyRecipeRepository : RecipeRepository {
        var shouldFail = true

        override fun observeRecipes(): Flow<List<Recipe>> = flow {
            check(!shouldFail) { "sin conexion" }
            emit(MediterraneanSampleData.allRecipes)
        }
    }

    /** Salta los estados intermedios hasta el primer `Success` que cumpla [predicate]. */
    private suspend fun ReceiveTurbine<MealPickerUiState>.awaitSuccess(
        predicate: (MealPickerUiState.Success) -> Boolean = { true },
    ): MealPickerUiState.Success {
        while (true) {
            val item = awaitItem()
            if (item is MealPickerUiState.Success && predicate(item)) return item
        }
    }
}
