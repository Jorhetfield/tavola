package com.tavola.feature.recipes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import com.tavola.core.designsystem.recipe.RecipeRowModel
import com.tavola.core.designsystem.recipe.TavolaRecipeRow
import com.tavola.core.designsystem.theme.TavolaTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** How long a swiped row may stay out of view waiting for its recipe to leave the list before it slides back. */
private const val REMOVAL_GRACE_MILLIS = 1_500L

/** How far along its width a row has to be when it is let go for the recipe to be deleted; less and it slides back. */
private const val DELETE_THRESHOLD_FRACTION = 0.5f

private const val ARMED_ICON_SCALE = 1.3f

/**
 * A recipe row that is deleted by dragging it to the start at least halfway. The same action is offered to
 * accessibility services, which cannot swipe.
 *
 * It is a gesture of its own instead of Material's `SwipeToDismissBox` on purpose:
 * - The decision is made only by where the row is when it is let go ([DELETE_THRESHOLD_FRACTION] of its width), never by
 *   the speed of the flick (Material jumps to the next anchor above 125 dp/s whatever the position) and never while the
 *   finger is still down.
 * - Nothing is saved across recompositions: Material's saved state is kept per list key, so a row that came back (Undo, or
 *   scrolling away and back) was restored as already swiped, which also blocked any new swipe.
 *
 * The row gives a tick and the bin grows as soon as it is far enough that letting go would delete it. Letting go there
 * deletes the recipe at once: the row keeps sliding away while it fades out, so it is already gone when the list closes the
 * gap. If the row is still in the list [REMOVAL_GRACE_MILLIS] after it was swept away (the delete failed) it slides back.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SwipeToRemoveRecipeRow(row: RecipeRowModel, onClick: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val motion = MaterialTheme.motionScheme
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val removeLabel = stringResource(R.string.recipe_list_remove)
    val currentOnRemove by rememberUpdatedState(onRemove)

    /** Horizontal position of the row, from 0 (in place) to minus its width (swept away). */
    val offsetX = remember { Animatable(0f) }

    /** 1 while the row is there, 0 once it has been let go past the threshold (it fades out while it slides). */
    val fade = remember { Animatable(1f) }
    var widthPx by remember { mutableIntStateOf(0) }
    var sweptAway by remember { mutableStateOf(false) }
    val armed by remember { derivedStateOf { isPastDeleteThreshold(offsetX.value, widthPx) } }
    val iconScale by animateFloatAsState(if (armed) ARMED_ICON_SCALE else 1f, motion.fastSpatialSpec(), label = "removeIconScale")

    LaunchedEffect(armed) { if (armed) haptics.performHapticFeedback(HapticFeedbackType.ContextClick) }
    LaunchedEffect(sweptAway) {
        if (sweptAway) {
            delay(REMOVAL_GRACE_MILLIS)
            launch { fade.animateTo(1f, motion.defaultEffectsSpec()) }
            offsetX.animateTo(0f, motion.defaultSpatialSpec())
            sweptAway = false
        }
    }

    val dragState = rememberDraggableState { delta ->
        // Undispatched, so the position is already up to date when the finger is lifted and the decision is taken.
        scope.launch(start = CoroutineStart.UNDISPATCHED) { offsetX.snapTo((offsetX.value + delta).coerceIn(-widthPx.toFloat(), 0f)) }
    }
    Box(
        modifier = modifier
            .onSizeChanged { size -> widthPx = size.width }
            .graphicsLayer { alpha = fade.value }
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(removeLabel) {
                        currentOnRemove()
                        true
                    },
                )
            }
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                enabled = !sweptAway,
                // The speed it is let go at is ignored: only where the row is decides.
                onDragStopped = { _ ->
                    if (armed) {
                        sweptAway = true
                        // Deleted as soon as the row is let go, not when it has finished sliding, and it fades out while it
                        // slides: there is no moment with the whole red background showing before the row leaves the list.
                        currentOnRemove()
                        launch { fade.animateTo(0f, motion.fastEffectsSpec()) }
                        offsetX.animateTo(-widthPx.toFloat(), motion.fastSpatialSpec())
                    } else {
                        offsetX.animateTo(0f, motion.defaultSpatialSpec())
                    }
                },
            ),
    ) {
        // Only while the row is being moved, so it never peeks out around the rounded corners at rest.
        if (offsetX.value < 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.large)
                    .padding(horizontal = TavolaTheme.spacing.xl),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
                )
            }
        }
        Box(modifier = Modifier.offset { IntOffset(x = offsetX.value.coerceAtMost(0f).roundToInt(), y = 0) }) {
            TavolaRecipeRow(model = row, onClick = onClick)
        }
    }
}

/** Whether a row dragged to [offsetPx] (negative towards the start) is far enough, out of a [widthPx] wide row, to be deleted. */
internal fun isPastDeleteThreshold(offsetPx: Float, widthPx: Int): Boolean = widthPx > 0 && offsetPx <= -widthPx * DELETE_THRESHOLD_FRACTION
