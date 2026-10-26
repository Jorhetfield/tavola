package com.tavola.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.calendar.TavolaWeekSelector
import com.tavola.core.designsystem.calendar.WeekSelectorModel
import com.tavola.core.designsystem.loading.TavolaLoadingIndicator
import com.tavola.core.designsystem.scaffold.TavolaScaffold
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.core.designsystem.topbar.TavolaTopBar
import com.tavola.core.designsystem.topbar.TavolaTopBarAction

/**
 * Landing de la app: calendario semanal.
 *
 * @param onOpenDebugMenu si no es `null` (solo en debug) añade un botón a la barra superior.
 * @param mealPicker contenido del selector de comida, que aporta `:app` (ver [MealPickerSlot]).
 */
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onOpenDebugMenu: (() -> Unit)? = null,
    mealPicker: MealPickerSlot? = null,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) {
        CalendarActions(
            onPreviousWeekClick = viewModel::onPreviousWeekClick,
            onNextWeekClick = viewModel::onNextWeekClick,
            onTodayClick = viewModel::onTodayClick,
            onRetryClick = viewModel::onRetryClick,
            onMealClick = viewModel::onMealClick,
            onMealPickerDismiss = viewModel::onMealPickerDismiss,
            onTaskCheckedChange = viewModel::onTaskCheckedChange,
            onTaskRemove = viewModel::onTaskRemove,
            onTaskRemoveUndo = viewModel::onTaskRemoveUndo,
            onTaskRemovedShown = viewModel::onTaskRemovedShown,
            onAddTaskClick = viewModel::onAddTaskClick,
            onAddTaskDismiss = viewModel::onAddTaskDismiss,
            onAddTaskConfirm = viewModel::onAddTaskConfirm,
        )
    }
    CalendarContent(state, actions, modifier, onOpenDebugMenu, mealPicker)
}

/** Parte sin estado de la pantalla: recibe el estado y las acciones, así se puede previsualizar y testear. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarContent(
    state: CalendarUiState,
    actions: CalendarActions,
    modifier: Modifier = Modifier,
    onOpenDebugMenu: (() -> Unit)? = null,
    mealPicker: MealPickerSlot? = null,
) {
    val topBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    RemovedTaskSnackbarEffect(
        removedTask = (state as? CalendarUiState.Success)?.removedTask,
        snackbarHostState = snackbarHostState,
        onUndo = actions.onTaskRemoveUndo,
        onShown = actions.onTaskRemovedShown,
    )
    TavolaScaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = { CalendarTopBar(state, actions, onOpenDebugMenu, topBarScroll) },
        reservesFloatingBarSpace = true,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        when (state) {
            CalendarUiState.Loading -> CenteredState(contentPadding) {
                TavolaLoadingIndicator(label = stringResource(R.string.calendar_loading))
            }

            CalendarUiState.Error -> CenteredState(contentPadding) { CalendarError(actions.onRetryClick) }

            is CalendarUiState.Success -> WeekContent(state, actions, contentPadding, mealPicker)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(
    state: CalendarUiState,
    actions: CalendarActions,
    onOpenDebugMenu: (() -> Unit)?,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val locale = rememberLocale()
    val success = state as? CalendarUiState.Success
    val debugAction = onOpenDebugMenu?.let { openDebugMenu ->
        TavolaTopBarAction(Icons.Rounded.BugReport, R.string.calendar_debug_menu, openDebugMenu)
    }

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        TavolaTopBar(
            title = success?.let { loaded -> monthYearTitle(loaded.weekStart, locale) } ?: stringResource(R.string.calendar_title),
            subtitle = success?.let { loaded -> stringResource(R.string.calendar_week_number, isoWeekNumber(loaded.weekStart)) },
            actions = listOfNotNull(debugAction),
            scrollBehavior = scrollBehavior,
        )
        if (success != null) {
            TavolaWeekSelector(
                model = WeekSelectorModel(label = weekRangeLabel(success.weekStart, locale), isCurrentWeek = success.isCurrentWeek),
                onPreviousClick = actions.onPreviousWeekClick,
                onNextClick = actions.onNextWeekClick,
                onTodayClick = actions.onTodayClick,
                modifier = Modifier.padding(horizontal = TavolaTheme.spacing.lg, vertical = TavolaTheme.spacing.sm),
            )
        }
    }
}

@Composable
private fun CenteredState(contentPadding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
