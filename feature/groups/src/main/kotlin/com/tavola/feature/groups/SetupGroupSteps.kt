package com.tavola.feature.groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import com.tavola.core.designsystem.badge.TavolaIconBadge
import com.tavola.core.designsystem.field.TavolaTextField
import com.tavola.core.designsystem.theme.TavolaTheme

private const val CONTENT_MAX_WIDTH_STEPS = 10

/** The choice: create a group or join one. */
@Composable
internal fun ChooseStep(actions: SetupGroupActions) {
    val spacing = TavolaTheme.spacing
    StepColumn {
        TavolaIconBadge(Icons.Rounded.Groups)
        Spacer(Modifier.height(spacing.lg))
        Text(
            text = stringResource(R.string.group_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = stringResource(R.string.group_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.xl))
        OptionCard(
            icon = Icons.Rounded.GroupAdd,
            title = stringResource(R.string.group_option_create_title),
            subtitle = stringResource(R.string.group_option_create_subtitle),
            onClick = actions.onCreateOptionClick,
        )
        Spacer(Modifier.height(spacing.md))
        OptionCard(
            icon = Icons.Rounded.VpnKey,
            title = stringResource(R.string.group_option_join_title),
            subtitle = stringResource(R.string.group_option_join_subtitle),
            onClick = actions.onJoinOptionClick,
        )
    }
}

/** The step where the new group is named. */
@Composable
internal fun CreateStep(state: SetupGroupUiState, actions: SetupGroupActions) {
    StepWithTitle(
        title = stringResource(R.string.group_create_title),
        subtitle = stringResource(R.string.group_create_hint),
    ) {
        TavolaTextField(
            value = state.name,
            onValueChange = actions.onNameChange,
            label = stringResource(R.string.group_name_label),
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Done,
            onImeAction = { if (state.canCreate) actions.onCreateClick() },
            isError = state.error != null,
        )
        SetupError(state.error)
        ConfirmButton(
            text = stringResource(if (state.isWorking) R.string.group_creating else R.string.group_create_confirm),
            enabled = state.canCreate,
            onClick = actions.onCreateClick,
        )
    }
}

/** The step where the code of an existing group is typed. */
@Composable
internal fun JoinStep(state: SetupGroupUiState, actions: SetupGroupActions) {
    StepWithTitle(
        title = stringResource(R.string.group_join_title),
        subtitle = stringResource(R.string.group_join_hint),
    ) {
        TavolaTextField(
            value = state.code,
            onValueChange = actions.onCodeChange,
            label = stringResource(R.string.group_code_label),
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Done,
            onImeAction = { if (state.canJoin) actions.onJoinClick() },
            capitalization = KeyboardCapitalization.Characters,
            isError = state.error != null,
        )
        SetupError(state.error)
        ConfirmButton(
            text = stringResource(if (state.isWorking) R.string.group_joining else R.string.group_join_confirm),
            enabled = state.canJoin,
            onClick = actions.onJoinClick,
        )
    }
}

@Composable
private fun StepColumn(content: @Composable ColumnScope.() -> Unit) {
    val spacing = TavolaTheme.spacing
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .widthIn(max = spacing.xxxl * CONTENT_MAX_WIDTH_STEPS)
            .padding(horizontal = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content,
    )
}

/** A step with a title and a line that says what to do, and then [content] (the field and the button). */
@Composable
private fun StepWithTitle(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    val spacing = TavolaTheme.spacing
    StepColumn {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(spacing.lg))
        content()
    }
}

@Composable
private fun OptionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val spacing = TavolaTheme.spacing
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConfirmButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = TavolaTheme.spacing.lg),
    ) {
        Text(text)
    }
}

/** Why the last attempt failed, below the field; it is announced to screen readers when it appears. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SetupError(error: SetupGroupError?) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = error != null,
        enter = expandVertically(motion.defaultSpatialSpec<IntSize>()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec<IntSize>()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        // The text of the error that was last shown stays while it fades out.
        val message = error?.let { shown -> stringResource(shown.messageRes()) }.orEmpty()
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = TavolaTheme.spacing.md)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

private fun SetupGroupError.messageRes(): Int = when (this) {
    SetupGroupError.Network -> R.string.group_error_network
    SetupGroupError.InvalidCode -> R.string.group_error_invalid_code
    SetupGroupError.Other -> R.string.group_error_other
}
