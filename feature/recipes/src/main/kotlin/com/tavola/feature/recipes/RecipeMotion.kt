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
import kotlinx.coroutines.delay

private const val STAGGER_STEP_MILLIS = 30L
private const val MAX_STAGGERED_ITEMS = 8
private const val POP_START_SCALE = 0.6f
private val EntranceLift = 12.dp
private val WIGGLE_DEGREES = listOf(-12f, 10f, -6f, 3f, 0f)

/** How long the item at [index] waits before entering, so a list opens as a short cascade and never drags on. */
internal fun entranceDelayMillis(index: Int): Long = index.coerceIn(0, MAX_STAGGERED_ITEMS) * STAGGER_STEP_MILLIS

/**
 * Fades the item in while it rises a little, after the delay for its [index]. It plays only the first time a [key] shows
 * up in [played] (a set that lives as long as the list), so scrolling back or re-filtering never replays it. A `null`
 * [played] means the screen was already shown before, so nothing plays.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun Modifier.staggeredEntrance(key: Any, index: Int, played: MutableSet<Any>?): Modifier {
    val motion = MaterialTheme.motionScheme
    val liftPx = with(LocalDensity.current) { EntranceLift.toPx() }
    val inspecting = LocalInspectionMode.current
    val firstTime = remember(key) { played != null && played.add(key) && !inspecting }
    val opacity = remember(key) { Animatable(if (firstTime) 0f else 1f) }
    val lift = remember(key) { Animatable(if (firstTime) 1f else 0f) }

    LaunchedEffect(key) {
        if (firstTime) {
            delay(entranceDelayMillis(index))
            opacity.animateTo(1f, motion.defaultEffectsSpec())
        }
    }
    LaunchedEffect(key) {
        if (firstTime) {
            delay(entranceDelayMillis(index))
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
