package com.tavola.core.designsystem.field

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/**
 * The reason something failed, below the field or the button that caused it. It opens and closes with the motion scheme, keeps
 * the last message while it closes, and is announced to screen readers when it appears or changes. Without a [message] it takes
 * no room.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaErrorMessage(message: String?, modifier: Modifier = Modifier, textAlign: TextAlign? = null) {
    val motion = MaterialTheme.motionScheme
    var lastMessage by remember { mutableStateOf("") }
    LaunchedEffect(message) { if (message != null) lastMessage = message }

    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = expandVertically(motion.defaultSpatialSpec<IntSize>()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec<IntSize>()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        Text(
            text = message ?: lastMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = textAlign,
            modifier = Modifier
                .padding(top = TavolaTheme.spacing.md)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

@TavolaPreviews
@Composable
private fun TavolaErrorMessagePreview() {
    TavolaTheme { TavolaErrorMessage(message = "No hay conexión. Comprueba tu red e inténtalo de nuevo.") }
}
