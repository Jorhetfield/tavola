package com.tavola.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.tavola.core.designsystem.snackbar.TavolaSnackbarHost
import com.tavola.core.designsystem.snackbar.TavolaUndoSnackbarEffect
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.core.designsystem.topbar.TavolaTopBar
import com.tavola.core.designsystem.topbar.TavolaTopBarAction

/**
 * The landing screen of the app: the weekly calendar.
 *
 * @param onOpenDebugMenu if not `null` (debug builds only) adds a button to the top bar.
 * @param mealPicker content of the meal picker, which `:app` provides (see [MealPickerSlot]).
 * @param onOpenRecipe asks `:app` to show a recipe; a meal of a day that is over opens its recipe this way.
 * @param onOpenGroup if not `null` adds a button to the top bar that asks `:app` to show the group of the person.
 */
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onOpenDebugMenu: (() -> Unit)? = null,
    mealPicker: MealPickerSlot? = null,
    onOpenRecipe: (recipeId: String) -> Unit = {},
    onOpenGroup: (() -> Unit)? = null,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    CalendarContent(state, actions, modifier, onOpenDebugMenu, mealPicker, onOpenRecipe, onOpenGroup)
}

/** Stateless part of the screen: it receives the state and the actions, so it can be previewed and tested. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarContent(
    state: CalendarUiState,
    actions: CalendarActions,
    modifier: Modifier = Modifier,
    onOpenDebugMenu: (() -> Unit)? = null,
    mealPicker: MealPickerSlot? = null,
    onOpenRecipe: (recipeId: String) -> Unit = {},
    onOpenGroup: (() -> Unit)? = null,
) {
    val topBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    TavolaUndoSnackbarEffect(
        removed = (state as? CalendarUiState.Success)?.removedTask,
        message = stringResource(R.string.calendar_task_removed),
        undoLabel = stringResource(R.string.calendar_task_undo),
        hostState = snackbarHostState,
        onUndo = actions.onTaskRemoveUndo,
        onShown = actions.onTaskRemovedShown,
    )
    TavolaScaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = { CalendarTopBar(state, actions, onOpenGroup, onOpenDebugMenu, topBarScroll) },
        reservesFloatingBarSpace = true,
        snackbarHost = { TavolaSnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        when (state) {
            CalendarUiState.Loading -> CenteredState(contentPadding) {
                TavolaLoadingIndicator(label = stringResource(R.string.calendar_loading))
            }

            CalendarUiState.Error -> CenteredState(contentPadding) { CalendarError(actions.onRetryClick) }

            is CalendarUiState.Success -> WeekContent(state, actions, contentPadding, mealPicker, onOpenRecipe)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(
    state: CalendarUiState,
    actions: CalendarActions,
    onOpenGroup: (() -> Unit)?,
    onOpenDebugMenu: (() -> Unit)?,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val locale = rememberLocale()
    val success = state as? CalendarUiState.Success
    val groupAction = onOpenGroup?.let { openGroup ->
        TavolaTopBarAction(Icons.Rounded.Groups, R.string.calendar_group, openGroup)
    }
    val debugAction = onOpenDebugMenu?.let { openDebugMenu ->
        TavolaTopBarAction(Icons.Rounded.BugReport, R.string.calendar_debug_menu, openDebugMenu)
    }

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        TavolaTopBar(
            title = success?.let { loaded -> monthYearTitle(loaded.weekStart, locale) } ?: stringResource(R.string.calendar_title),
            subtitle = success?.let { loaded -> stringResource(R.string.calendar_week_number, isoWeekNumber(loaded.weekStart)) },
            actions = listOfNotNull(groupAction, debugAction),
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
