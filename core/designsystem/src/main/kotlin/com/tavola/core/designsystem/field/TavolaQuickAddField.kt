package com.tavola.core.designsystem.field

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private val FocusBorderWidth = 2.dp

/**
 * Pill-shaped field to add things one after another (an item of the shopping list): type and press the keyboard's action or the
 * round button on the end, which only lights up when there is something to add. Focusing it draws a primary outline that grows
 * with a spring, like [TavolaSearchField]. The field keeps the focus after adding, so the next one can be typed at once.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaQuickAddField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    addContentDescription: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val outlineColor by animateColorAsState(
        targetValue = if (focused) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = motion.defaultEffectsSpec(),
        label = "quickAddOutlineColor",
    )
    val outlineWidth by animateDpAsState(
        targetValue = if (focused) FocusBorderWidth else 0.dp,
        animationSpec = motion.defaultSpatialSpec(),
        label = "quickAddOutlineWidth",
    )
    val canAdd = value.isNotBlank()

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .border(outlineWidth, outlineColor, CircleShape),
        placeholder = { Text(placeholder) },
        trailingIcon = {
            FilledIconButton(onClick = onAdd, enabled = canAdd, modifier = Modifier.padding(end = TavolaTheme.spacing.xs)) {
                Icon(Icons.Rounded.Add, contentDescription = addContentDescription)
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = tavolaFieldColors(),
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Sentences),
        keyboardActions = KeyboardActions(onDone = { if (canAdd) onAdd() }),
    )
}

@TavolaPreviews
@Composable
private fun TavolaQuickAddFieldPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
        ) {
            TavolaQuickAddField(value = "", onValueChange = {
            }, placeholder = "Añadir a la lista", addContentDescription = "Añadir", onAdd = {})
            TavolaQuickAddField(value = "Leche", onValueChange = {
            }, placeholder = "Añadir a la lista", addContentDescription = "Añadir", onAdd = {})
        }
    }
}
