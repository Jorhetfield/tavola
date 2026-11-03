package com.tavola.feature.groups

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tavola.core.designsystem.dialog.TavolaConfirmDialog
import com.tavola.core.designsystem.dialog.TavolaTextInputDialog

/** The dialog that is open on the screen: rename the group, renew its code or leave it. */
@Composable
internal fun MyGroupDialogs(state: MyGroupUiState.Success, actions: MyGroupActions) {
    when (state.dialog) {
        null -> Unit

        MyGroupDialog.Rename -> TavolaTextInputDialog(
            title = stringResource(R.string.group_rename_title),
            label = stringResource(R.string.group_name_label),
            confirmText = stringResource(R.string.group_rename_confirm),
            dismissText = stringResource(R.string.group_cancel),
            onConfirm = actions.onRenameConfirm,
            onDismiss = actions.onDialogDismiss,
            initialText = state.group.name,
        )

        MyGroupDialog.RenewCode -> TavolaConfirmDialog(
            title = stringResource(R.string.group_renew_title),
            message = stringResource(R.string.group_renew_message),
            confirmText = stringResource(R.string.group_renew_confirm),
            dismissText = stringResource(R.string.group_cancel),
            onConfirm = actions.onRenewConfirm,
            onDismiss = actions.onDialogDismiss,
        )

        MyGroupDialog.Leave -> TavolaConfirmDialog(
            title = stringResource(R.string.group_leave_title),
            message = stringResource(R.string.group_leave_message, state.group.name),
            confirmText = stringResource(R.string.group_leave_confirm),
            dismissText = stringResource(R.string.group_cancel),
            onConfirm = actions.onLeaveConfirm,
            onDismiss = actions.onDialogDismiss,
            isDestructive = true,
        )
    }
}
