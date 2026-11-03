package com.tavola.feature.groups

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.loading.TavolaFullScreenLoading
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/**
 * Shows [content] (the app) to someone who belongs to a group and the first step (create a group or join one) to someone who does
 * not yet. The change between them fades with the motion scheme. Put it inside the sign-in gate: it needs a signed-in person.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GroupGate(modifier: Modifier = Modifier, viewModel: GroupGateViewModel = hiltViewModel(), content: @Composable () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val motion = MaterialTheme.motionScheme

    AnimatedContent(
        targetState = state,
        modifier = modifier,
        transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
        label = "groupGate",
    ) { current ->
        when (current) {
            GroupGateUiState.Loading -> TavolaFullScreenLoading(label = stringResource(R.string.group_loading))
            GroupGateUiState.NoGroup -> SetupGroupScreen()
            GroupGateUiState.HasGroup -> content()
            GroupGateUiState.Error -> GroupGateError(onRetryClick = viewModel::onRetryClick)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun GroupGateError(onRetryClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.systemBarsPadding().padding(horizontal = spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Text(
                text = stringResource(R.string.group_gate_error),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRetryClick, shapes = ButtonDefaults.shapes()) { Text(stringResource(R.string.group_gate_retry)) }
        }
    }
}

@TavolaPreviews
@Composable
private fun GroupGateErrorPreview() {
    TavolaTheme { GroupGateError(onRetryClick = {}) }
}
