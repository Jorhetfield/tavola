package com.tavola.feature.groups

/** Everything the "My group" screen can ask of its ViewModel. Does nothing by default (previews and screenshots). */
data class MyGroupActions(
    val onRenameClick: () -> Unit = {},
    val onRenewClick: () -> Unit = {},
    val onLeaveClick: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onRenameConfirm: (name: String) -> Unit = {},
    val onRenewConfirm: () -> Unit = {},
    val onLeaveConfirm: () -> Unit = {},
    val onCodeCopied: () -> Unit = {},
    val onCodeCopiedShown: () -> Unit = {},
    val onFailureShown: () -> Unit = {},
    val onRetryClick: () -> Unit = {},
)

/**
 * The actions of the screen wired to this ViewModel. Every action that the screen can ask for has to be here: the defaults of
 * [MyGroupActions] do nothing, so one left out would compile and simply not work (`MyGroupActionsTest` checks it).
 */
internal fun MyGroupViewModel.toActions(): MyGroupActions = MyGroupActions(
    onRenameClick = ::onRenameClick,
    onRenewClick = ::onRenewClick,
    onLeaveClick = ::onLeaveClick,
    onDialogDismiss = ::onDialogDismiss,
    onRenameConfirm = ::onRenameConfirm,
    onRenewConfirm = ::onRenewConfirm,
    onLeaveConfirm = ::onLeaveConfirm,
    onCodeCopied = ::onCodeCopied,
    onCodeCopiedShown = ::onCodeCopiedShown,
    onFailureShown = ::onFailureShown,
    onRetryClick = ::onRetryClick,
)
