package com.tavola.feature.recipes

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.motion.PlayedEntrances
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val STAGGER_STEP_MILLIS = 50L
private const val MAX_STAGGERED_ITEMS = 8
private const val POP_START_SCALE = 0.6f
private val EntranceLift = 12.dp
private val WIGGLE_DEGREES = listOf(-12f, 10f, -6f, 3f, 0f)

/** How long the item at [index] waits before entering, so a list opens as a short cascade and never drags on. */
internal fun entranceDelayMillis(index: Int): Long = index.coerceIn(0, MAX_STAGGERED_ITEMS) * STAGGER_STEP_MILLIS

/**
 * Only the first rows of a list enter in the cascade, the ones on screen when it opens. A row further down is composed
 * later, when the user scrolls to it, and animating it then would leave it invisible for the cascade delay and then
 * lift it into place under the finger.
 */
internal fun takesPartInCascade(index: Int): Boolean = index in 0..MAX_STAGGERED_ITEMS

/**
 * Fades the item in while it rises a little, after the delay for its [index]. It plays only the first time a [key]
 * is asked for in [played], so scrolling back, re-filtering or coming back to the screen never replays it. Items beyond
 * the cascade ([takesPartInCascade]) are simply shown.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun Modifier.staggeredEntrance(key: String, index: Int, played: PlayedEntrances): Modifier {
    val motion = MaterialTheme.motionScheme
    val liftPx = with(LocalDensity.current) { EntranceLift.toPx() }
    val inspecting = LocalInspectionMode.current
    val firstTime = remember(key) { !inspecting && takesPartInCascade(index) && played.isFirstTime(key) }
    val opacity = remember(key) { Animatable(if (firstTime) 0f else 1f) }
    val lift = remember(key) { Animatable(if (firstTime) 1f else 0f) }

    LaunchedEffect(key) {
        if (firstTime) {
            delay(entranceDelayMillis(index))
            launch { opacity.animateTo(1f, motion.defaultEffectsSpec()) }
            lift.animateTo(0f, motion.defaultSpatialSpec())
        }
    }
    return graphicsLayer {
        alpha = opacity.value
        translationY = lift.value * liftPx
    }
}

/** Grows from a smaller size to its own with a spatial spring once, after [delayMillis]. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun Modifier.popOnAppear(delayMillis: Long): Modifier {
    val motion = MaterialTheme.motionScheme
    val inspecting = LocalInspectionMode.current
    val scale = remember { Animatable(if (inspecting) 1f else POP_START_SCALE) }
    LaunchedEffect(Unit) {
        if (!inspecting) {
            delay(delayMillis)
            scale.animateTo(1f, motion.defaultSpatialSpec())
        }
    }
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        alpha = ((scale.value - POP_START_SCALE) / (1f - POP_START_SCALE)).coerceIn(0f, 1f)
    }
}

/** A short sway when it shows up, like a "nothing here" shake of the head. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun Modifier.wiggleOnAppear(): Modifier {
    val motion = MaterialTheme.motionScheme
    val rotation = remember { Animatable(0f) }
    val inspecting = LocalInspectionMode.current
    LaunchedEffect(Unit) {
        if (!inspecting) WIGGLE_DEGREES.forEach { degrees -> rotation.animateTo(degrees, motion.fastSpatialSpec()) }
    }
    return graphicsLayer { rotationZ = rotation.value }
}
