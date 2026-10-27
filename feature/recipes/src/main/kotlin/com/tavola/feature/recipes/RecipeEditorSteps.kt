package com.tavola.feature.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.field.TavolaTextField
import com.tavola.core.designsystem.theme.TavolaTheme

/** Title, one card per step (with move up / down) and the add button. */
internal fun LazyListScope.stepsSection(steps: List<StepDraft>, actions: RecipeEditorActions) {
    item(key = STEPS_TITLE_KEY) { EditorSectionTitle(R.string.recipe_detail_steps, editorItemMotion()) }
    itemsIndexed(steps, key = { _, step -> "step:${step.key}" }) { index, step ->
        StepCard(step, number = index + 1, isFirst = index == 0, isLast = index == steps.lastIndex, actions, editorItemMotion())
    }
    item(key = STEPS_ADD_KEY) { AddRowButton(R.string.recipe_editor_step_add, actions.onStepAdd, editorItemMotion()) }
}

@Composable
private fun StepCard(
    step: StepDraft,
    number: Int,
    isFirst: Boolean,
    isLast: Boolean,
    actions: RecipeEditorActions,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val spacing = TavolaTheme.spacing
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier.padding(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            StepNumberBadge(number = number, modifier = Modifier.padding(top = spacing.md))
            TavolaTextField(
                value = step.text,
                onValueChange = { text -> actions.onStepChange(step.key, text) },
                label = stringResource(R.string.recipe_editor_step_label, number),
                modifier = Modifier.weight(1f),
                singleLine = false,
                onImeAction = { focusManager.clearFocus() },
            )
            Column {
                IconButton(onClick = { actions.onStepMove(step.key, -1) }, enabled = !isFirst) {
                    Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = stringResource(R.string.recipe_editor_step_move_up))
                }
                IconButton(onClick = { actions.onStepMove(step.key, 1) }, enabled = !isLast) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = stringResource(R.string.recipe_editor_step_move_down))
                }
                IconButton(onClick = { actions.onStepRemove(step.key) }) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.recipe_editor_step_remove))
                }
            }
        }
    }
}

private const val STEPS_TITLE_KEY = "steps-title"
private const val STEPS_ADD_KEY = "steps-add"
