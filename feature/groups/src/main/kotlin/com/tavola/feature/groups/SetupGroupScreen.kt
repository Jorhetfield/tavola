package com.tavola.feature.groups

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.preview.TavolaAdaptivePreviews
import com.tavola.core.designsystem.preview.TavolaFontScalePreviews
import com.tavola.core.designsystem.scaffold.TavolaScaffold
import com.tavola.core.designsystem.theme.TavolaTheme

/** The first step for someone with no group: create one or join one with a code. */
@Composable
fun SetupGroupScreen(modifier: Modifier = Modifier, viewModel: SetupGroupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    SetupGroupContent(state, actions, modifier)
}

/** Stateless part of the screen, so it can be previewed and shown in the screenshots. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupGroupContent(state: SetupGroupUiState, actions: SetupGroupActions, modifier: Modifier = Modifier) {
    val motion = MaterialTheme.motionScheme
    // The system back gesture goes back to the choice from the two steps, as the arrow does.
    BackHandler(enabled = state.step != SetupStep.Choose && !state.isWorking, onBack = actions.onBackClick)

    TavolaScaffold(modifier = modifier) { _ ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
                label = "setupStep",
            ) { step ->
                when (step) {
                    SetupStep.Choose -> ChooseStep(actions)
                    SetupStep.Create -> CreateStep(state, actions)
                    SetupStep.Join -> JoinStep(state, actions)
                }
            }
            // The arrow stays at the top corner like in any screen, while the content of the step is centered.
            AnimatedVisibility(
                visible = state.step != SetupStep.Choose,
                modifier = Modifier.align(Alignment.TopStart).systemBarsPadding(),
                enter = fadeIn(motion.defaultEffectsSpec()),
                exit = fadeOut(motion.fastEffectsSpec()),
            ) {
                IconButton(onClick = actions.onBackClick, enabled = !state.isWorking) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.group_back))
                }
            }
        }
    }
}

@TavolaAdaptivePreviews
@Composable
private fun SetupGroupChoosePreview() {
    TavolaTheme { SetupGroupContent(SetupGroupUiState(), SetupGroupActions()) }
}

@TavolaFontScalePreviews
@Composable
private fun SetupGroupJoinFontScalePreview() {
    TavolaTheme {
        SetupGroupContent(
            SetupGroupUiState(step = SetupStep.Join, code = "K7M2QX", error = SetupGroupError.InvalidCode),
            SetupGroupActions(),
        )
    }
}
