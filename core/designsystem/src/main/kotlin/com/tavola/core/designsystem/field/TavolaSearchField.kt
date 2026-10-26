package com.tavola.core.designsystem.field

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private val FocusBorderWidth = 2.dp
private const val ICON_SWAP_SCALE = 0.6f

/**
 * Pill-shaped search field. Focusing it draws a primary outline that grows with a spring and turns the magnifier into
 * a back arrow that leaves the search; the clear button enters and leaves with a spring (scale + opacity) while
 * typing. [onSearch] is called with the keyboard action (for example to hide it).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    clearContentDescription: String,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
) {
    val motion = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val outlineColor by animateColorAsState(
        targetValue = if (focused) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = motion.defaultEffectsSpec(),
        label = "searchOutlineColor",
    )
    val outlineWidth by animateDpAsState(
        targetValue = if (focused) FocusBorderWidth else 0.dp,
        animationSpec = motion.defaultSpatialSpec(),
        label = "searchOutlineWidth",
    )

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .border(outlineWidth, outlineColor, CircleShape),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            AnimatedContent(
                targetState = focused,
                transitionSpec = {
                    (fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.fastSpatialSpec(), initialScale = ICON_SWAP_SCALE)) togetherWith
                        (fadeOut(motion.fastEffectsSpec()) + scaleOut(motion.fastSpatialSpec(), targetScale = ICON_SWAP_SCALE))
                },
                label = "searchLeadingIcon",
            ) { isFocused ->
                if (isFocused) {
                    IconButton(onClick = { focusManager.clearFocus() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.search_field_stop))
                    }
                } else {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                }
            }
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = value.isNotEmpty(),
                enter = fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.fastSpatialSpec()),
                exit = fadeOut(motion.defaultEffectsSpec()) + scaleOut(motion.fastSpatialSpec()),
            ) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = clearContentDescription)
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = tavolaFieldColors(),
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    )
}

@TavolaPreviews
@Composable
private fun TavolaSearchFieldPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
        ) {
            TavolaSearchField(value = "", onValueChange = {}, placeholder = "Buscar receta", clearContentDescription = "Borrar")
            TavolaSearchField(value = "paella", onValueChange = {}, placeholder = "Buscar receta", clearContentDescription = "Borrar")
        }
    }
}
