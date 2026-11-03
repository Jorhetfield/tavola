package com.tavola.core.designsystem.calendar

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private val AddIconSize = 18.dp
private val OutlineWidth = 1.5.dp
private const val OUTLINE_ALPHA = 0.4f

/**
 * "Add task" action of a day. It is the last chip of the task list, so the action sits next to what it adds to, and it is
 * drawn as an outline (no fill) to tell an action from a task. It is exactly as tall as a task chip (the outline sits inside
 * its height), so the two line up on the same row. The press response comes from the ripple.
 */
@Composable
fun TavolaAddTaskChip(onClick: () -> Unit, modifier: Modifier = Modifier, contentColor: Color = TavolaTheme.colors.onDayCard) {
    val spacing = TavolaTheme.spacing
    Row(
        modifier = modifier
            .clip(CircleShape)
            .border(OutlineWidth, contentColor.copy(alpha = OUTLINE_ALPHA), CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = TaskChipMinHeight)
            .padding(horizontal = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = contentColor, modifier = Modifier.size(AddIconSize))
        Text(
            text = stringResource(R.string.day_card_add_task),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

@TavolaPreviews
@Composable
private fun TavolaAddTaskChipPreview() {
    TavolaTheme {
        TavolaAddTaskChip(onClick = {}, modifier = Modifier.padding(TavolaTheme.spacing.lg))
    }
}
