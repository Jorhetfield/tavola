package com.tavola.feature.recipes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import kotlinx.datetime.LocalDate

private enum class PickerMode { Recipes, Quick }

private const val SLIDE_DIVISOR = 3

/**
 * Contenido de la hoja para elegir qué se come en un hueco: cabecera con el día, un conmutador conectado
 * (Recetas / Comida rápida) y el panel correspondiente. Es la parte sin estado: recibe el estado y las acciones.
 *
 * @param current lo que ocupa ahora el hueco (`null` si está vacío): se marca en la lista y activa "Quitar".
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MealPickerContent(
    date: LocalDate,
    type: MealType,
    current: PlannedMeal?,
    state: MealPickerUiState,
    actions: MealPickerActions,
    modifier: Modifier = Modifier,
) {
    val spacing = TavolaTheme.spacing
    val motion = MaterialTheme.motionScheme
    val currentQuick = current as? PlannedMeal.Quick
    var mode by rememberSaveable { mutableStateOf(if (currentQuick != null) PickerMode.Quick else PickerMode.Recipes) }
    var quickTitle by rememberSaveable { mutableStateOf(currentQuick?.title.orEmpty()) }
    var quickNote by rememberSaveable { mutableStateOf(currentQuick?.note.orEmpty()) }
    val selectedRecipeId = (current as? PlannedMeal.FromRecipe)?.recipe?.recipeId

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg)
            .padding(bottom = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        PickerHeader(date = date, type = type, hasMeal = current != null, onClearClick = actions.onClearClick)
        ModeToggle(mode = mode, onModeChange = { newMode -> mode = newMode })
        AnimatedContent(
            targetState = mode,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val direction = if (targetState == PickerMode.Quick) 1 else -1
                val slideIn = slideInHorizontally(motion.defaultSpatialSpec()) { fullWidth -> direction * fullWidth / SLIDE_DIVISOR }
                val slideOut = slideOutHorizontally(motion.defaultSpatialSpec()) { fullWidth -> -direction * fullWidth / SLIDE_DIVISOR }
                (slideIn + fadeIn(motion.defaultEffectsSpec())) togetherWith (slideOut + fadeOut(motion.fastEffectsSpec()))
            },
            label = "pickerMode",
        ) { target ->
            when (target) {
                PickerMode.Recipes -> RecipesPane(
                    state = state,
                    selectedRecipeId = selectedRecipeId,
                    actions = actions,
                    onWriteInstead = { text ->
                        quickTitle = text
                        mode = PickerMode.Quick
                    },
                )

                PickerMode.Quick -> QuickMealPane(
                    title = quickTitle,
                    note = quickNote,
                    onTitleChange = { newTitle -> quickTitle = newTitle },
                    onNoteChange = { newNote -> quickNote = newNote },
                    onConfirm = { actions.onQuickConfirm(quickTitle, quickNote) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PickerHeader(date: LocalDate, type: MealType, hasMeal: Boolean, onClearClick: () -> Unit) {
    val locale = rememberLocale()
    val resources = LocalResources.current
    val dateLabel = remember(date, locale) { mealDateLabel(date, locale, resources) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(if (type == MealType.Lunch) R.string.meal_picker_title_lunch else R.string.meal_picker_title_dinner),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (hasMeal) {
            TextButton(
                onClick = onClearClick,
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = null)
                Text(
                    text = stringResource(R.string.meal_picker_clear),
                    modifier = Modifier.padding(start = TavolaTheme.spacing.xs),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModeToggle(mode: PickerMode, onModeChange: (PickerMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        ToggleButton(
            checked = mode == PickerMode.Recipes,
            onCheckedChange = { onModeChange(PickerMode.Recipes) },
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.Rounded.MenuBook, contentDescription = null)
            Text(text = stringResource(R.string.meal_picker_mode_recipes), modifier = Modifier.padding(start = TavolaTheme.spacing.sm))
        }
        ToggleButton(
            checked = mode == PickerMode.Quick,
            onCheckedChange = { onModeChange(PickerMode.Quick) },
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.Rounded.EditNote, contentDescription = null)
            Text(text = stringResource(R.string.meal_picker_mode_quick), modifier = Modifier.padding(start = TavolaTheme.spacing.sm))
        }
    }
}
