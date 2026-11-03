package com.tavola.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tavola.core.designsystem.member.TavolaMemberAvatar
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.InviteCode

/** The name of the group, with the button to change it. */
@Composable
internal fun GroupNameRow(name: String, enabled: Boolean, onRenameClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRenameClick, enabled = enabled) {
            Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.group_rename_description))
        }
    }
}

/** The invite code of the group, with what can be done with it: copy it, share it or renew it. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun InviteCodeCard(
    code: String,
    enabled: Boolean,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onRenewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = TavolaTheme.spacing
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(modifier = Modifier.padding(spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Text(text = stringResource(R.string.group_invite_title), style = MaterialTheme.typography.labelLarge)
            Text(text = InviteCode.format(code), style = MaterialTheme.typography.displaySmall)
            Text(text = stringResource(R.string.group_invite_hint), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm), modifier = Modifier.fillMaxWidth().padding(top = spacing.sm)) {
                FilledTonalButton(onClick = onCopyClick, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                    Text(text = stringResource(R.string.group_copy_code), modifier = Modifier.padding(start = spacing.sm))
                }
                Button(onClick = onShareClick, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Share, contentDescription = null)
                    Text(text = stringResource(R.string.group_share_code), modifier = Modifier.padding(start = spacing.sm))
                }
            }
            TextButton(
                onClick = onRenewClick,
                enabled = enabled,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Text(text = stringResource(R.string.group_renew_code), modifier = Modifier.padding(start = spacing.sm))
            }
        }
    }
}

/** A person of the group: their avatar, their name and a tag if it is the one using the app. */
@Composable
internal fun MemberRow(item: MemberItem, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        TavolaMemberAvatar(name = item.member.displayName, colorIndex = item.member.colorIndex)
        Column {
            Text(
                text = item.member.displayName ?: stringResource(R.string.group_member_unnamed),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (item.isMe) {
                Text(
                    text = stringResource(R.string.group_member_you),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** Leaving the group, drawn as a warning and not as a main action. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LeaveGroupButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.group_leave))
    }
}
