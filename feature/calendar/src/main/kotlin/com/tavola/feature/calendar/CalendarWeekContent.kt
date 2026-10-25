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

/** Lista de días de la semana. Al cambiar de semana, el contenido se desliza hacia el lado correspondiente. */
@Composable
internal fun WeekContent(state: CalendarUiState.Success, actions: CalendarActions, contentPadding: PaddingValues) {
    AnimatedContent(
        targetState = state,
        contentKey = { it.weekStart },
        transitionSpec = {
            val forward = targetState.weekStart > initialState.weekStart
            val direction = if (forward) 1 else -1
            (slideInHorizontally { direction * it / SLIDE_DIVISOR } + fadeIn()) togetherWith
                (slideOutHorizontally { -direction * it / SLIDE_DIVISOR } + fadeOut())
        },
        label = "week",
    ) { week ->
        DayList(week, actions, contentPadding)
    }

    state.addTaskDate?.let {
        TavolaAddTaskDialog(
            title = stringResource(R.string.calendar_add_task_title),
            label = stringResource(R.string.calendar_add_task_label),
            confirmText = stringResource(R.string.calendar_add_task_confirm),
            dismissText = stringResource(R.string.calendar_add_task_dismiss),
            onConfirm = actions.onAddTaskConfirm,
            onDismiss = actions.onAddTaskDismiss,
        )
    }
}

@Composable
private fun DayList(week: CalendarUiState.Success, actions: CalendarActions, contentPadding: PaddingValues) {
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
        items(cards, key = { it.id }) { card ->
            val date = LocalDate.parse(card.id)
            TavolaDayCard(
                model = card,
                onMealClick = { type -> actions.onMealClick(date, type.toDomain()) },
                onTaskCheckedChange = { taskId, checked -> actions.onTaskCheckedChange(date, taskId, checked) },
                onAddTaskClick = { actions.onAddTaskClick(date) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}
