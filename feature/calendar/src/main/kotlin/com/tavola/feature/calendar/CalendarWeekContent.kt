package com.tavola.feature.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.tavola.core.designsystem.calendar.TavolaDayCard
import com.tavola.core.designsystem.dialog.TavolaAddTaskDialog
import com.tavola.core.designsystem.theme.TavolaTheme
import kotlinx.datetime.LocalDate

private const val SLIDE_DIVISOR = 4

/** The days of the week. When the week changes, the content slides towards the matching side. */
@Composable
internal fun WeekContent(
    state: CalendarUiState.Success,
    actions: CalendarActions,
    contentPadding: PaddingValues,
    mealPicker: MealPickerSlot? = null,
    onOpenRecipe: (recipeId: String) -> Unit = {},
) {
    AnimatedContent(
        targetState = state,
        contentKey = { week -> week.weekStart },
        transitionSpec = {
            val forward = targetState.weekStart > initialState.weekStart
            val direction = if (forward) 1 else -1
            (slideInHorizontally { fullWidth -> direction * fullWidth / SLIDE_DIVISOR } + fadeIn()) togetherWith
                (slideOutHorizontally { fullWidth -> -direction * fullWidth / SLIDE_DIVISOR } + fadeOut())
        },
        label = "week",
    ) { week ->
        DayList(week, actions, contentPadding, onOpenRecipe)
    }

    state.mealPicker?.let { target ->
        if (mealPicker != null) MealPickerHost(target, actions.onMealPickerDismiss, mealPicker)
    }

    if (state.addTaskDate != null || state.editTask != null) {
        val editing = state.editTask
        TavolaAddTaskDialog(
            title = stringResource(if (editing == null) R.string.calendar_add_task_title else R.string.calendar_edit_task_title),
            label = stringResource(R.string.calendar_add_task_label),
            confirmText = stringResource(if (editing == null) R.string.calendar_add_task_confirm else R.string.calendar_edit_task_confirm),
            dismissText = stringResource(R.string.calendar_add_task_dismiss),
            onConfirm = actions.onTaskDialogConfirm,
            onDismiss = actions.onTaskDialogDismiss,
            initialText = editing?.text.orEmpty(),
        )
    }
}

@Composable
private fun DayList(
    week: CalendarUiState.Success,
    actions: CalendarActions,
    contentPadding: PaddingValues,
    onOpenRecipe: (recipeId: String) -> Unit,
) {
    val spacing = TavolaTheme.spacing
    val cards = rememberDayCardModels(week.days)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        items(cards, key = { cardModel -> cardModel.id }) { card ->
            val date = LocalDate.parse(card.id)
            TavolaDayCard(
                model = card,
                onMealClick = { type -> actions.onMealClick(date, type.toDomain()) },
                onRecipeClick = onOpenRecipe,
                onTaskCheckedChange = { taskId, checked -> actions.onTaskCheckedChange(date, taskId, checked) },
                onTaskRemoveClick = { taskId -> actions.onTaskRemove(date, taskId) },
                onTaskEditClick = { taskId -> actions.onTaskEditClick(date, taskId) },
                onAddTaskClick = { actions.onAddTaskClick(date) },
                onToggleExpanded = { actions.onDayToggle(date) },
            )
        }
    }
}
