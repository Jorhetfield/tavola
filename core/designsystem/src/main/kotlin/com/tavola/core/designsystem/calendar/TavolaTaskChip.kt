package com.tavola.core.designsystem.calendar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private val CheckIconSize = 20.dp
private val RemoveIconSize = 18.dp
private val RemoveTargetWidth = 40.dp
private val ChipMinHeight = 40.dp

/**
 * Tarea rápida del día ("sacar a descongelar", "comprar pimientos"). Se marca al pulsar el texto y se elimina
 * con la X de la derecha, que es una zona táctil aparte para no borrar por error al marcar. Las tareas hechas
 * no muestran la X.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaTaskChip(
    model: TaskChipModel,
    onCheckedChange: (checked: Boolean) -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TavolaTheme.colors
    val effects = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val container by animateColorAsState(if (model.done) colors.taskChipDone else colors.taskChip, effects, label = "taskChipContainer")
    val content by animateColorAsState(if (model.done) colors.onTaskChipDone else colors.onTaskChip, effects, label = "taskChipContent")

    Row(
        modifier = modifier
            .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec())
            .clip(CircleShape)
            .background(container)
            .heightIn(min = ChipMinHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TaskChipLabel(model, content, onCheckedChange, modifier = Modifier.weight(1f, fill = false))
        // Una tarea hecha no se elimina desde aquí: primero se desmarca.
        if (model.done) {
            Spacer(Modifier.width(TavolaTheme.spacing.lg))
        } else {
            TaskChipRemoveButton(model.text, content, onRemoveClick)
        }
    }
}

@Composable
private fun TaskChipLabel(model: TaskChipModel, content: Color, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    Row(
        modifier = modifier
            .clip(CircleShape)
            .toggleable(value = model.done, role = Role.Checkbox, onValueChange = onCheckedChange)
            .heightIn(min = ChipMinHeight)
            .padding(start = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Crossfade(targetState = model.done, label = "taskChipIcon") { done ->
            Icon(
                imageVector = if (done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(CheckIconSize),
            )
        }
        Text(
            text = model.text,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            textDecoration = if (model.done) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TaskChipRemoveButton(taskText: String, content: Color, onRemoveClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(RemoveTargetWidth)
            .heightIn(min = ChipMinHeight)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onRemoveClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.calendar_remove_task, taskText),
            tint = content,
            modifier = Modifier.size(RemoveIconSize),
        )
    }
}

@TavolaPreviews
@Composable
private fun TavolaTaskChipPreview() {
    TavolaTheme {
        FlowRow(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            TavolaTaskChip(TaskChipModel("1", "Sacar pollo a descongelar"), onCheckedChange = {}, onRemoveClick = {})
            TavolaTaskChip(TaskChipModel("2", "Comprar pimientos", done = true), onCheckedChange = {}, onRemoveClick = {})
        }
    }
}
