package com.tavola.feature.groups

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.R as DesignSystemR
import com.tavola.core.designsystem.loading.TavolaLoadingIndicator
import com.tavola.core.designsystem.scaffold.TavolaScaffold
import com.tavola.core.designsystem.snackbar.TavolaSnackbarHost
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.core.designsystem.topbar.TavolaTopBar
import com.tavola.core.designsystem.topbar.TavolaTopBarAction
import com.tavola.domain.model.Group
import com.tavola.domain.model.InviteCode
import kotlinx.coroutines.launch

/** The group of the person: its name, its invite code, its members, and what a member can do (rename, renew the code, leave). */
@Composable
fun MyGroupScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: MyGroupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    val context = LocalContext.current
    val resources = LocalResources.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    MyGroupContent(
        state = state,
        actions = actions,
        onBack = onBack,
        onCopyCode = { group ->
            val label = resources.getString(R.string.group_clipboard_label)
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, InviteCode.format(group.inviteCode))))
                actions.onCodeCopied()
            }
        },
        onShareCode = { group ->
            val text = resources.getString(R.string.group_share_text, group.name, InviteCode.format(group.inviteCode))
            val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
            context.startActivity(Intent.createChooser(send, resources.getString(R.string.group_share_chooser)))
        },
        modifier = modifier,
    )
}

/** Stateless part of the screen, so it can be previewed and shown in the screenshots. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGroupContent(
    state: MyGroupUiState,
    actions: MyGroupActions,
    onBack: () -> Unit,
    onCopyCode: (Group) -> Unit,
    onShareCode: (Group) -> Unit,
    modifier: Modifier = Modifier,
) {
    val topBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarMessages(state as? MyGroupUiState.Success, actions, snackbarHostState)

    TavolaScaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            TavolaTopBar(
                title = stringResource(R.string.group_my_title),
                navigationAction = TavolaTopBarAction(Icons.AutoMirrored.Rounded.ArrowBack, DesignSystemR.string.topbar_back, onBack),
                scrollBehavior = topBarScroll,
            )
        },
        snackbarHost = { TavolaSnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        when (state) {
            MyGroupUiState.Loading -> TavolaLoadingIndicator(modifier = Modifier.padding(contentPadding))

            MyGroupUiState.Error -> GroupGateError(onRetryClick = actions.onRetryClick, modifier = Modifier.padding(contentPadding))

            is MyGroupUiState.Success -> {
                MyGroupBody(state, actions, contentPadding, onCopyCode, onShareCode)
                MyGroupDialogs(state, actions)
            }
        }
    }
}

@Composable
private fun MyGroupBody(
    state: MyGroupUiState.Success,
    actions: MyGroupActions,
    contentPadding: PaddingValues,
    onCopyCode: (Group) -> Unit,
    onShareCode: (Group) -> Unit,
) {
    val spacing = TavolaTheme.spacing
    val enabled = !state.isWorking
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding() + spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item { GroupNameRow(state.group.name, enabled, actions.onRenameClick) }
        item {
            InviteCodeCard(
                code = state.group.inviteCode,
                enabled = enabled,
                onCopyClick = { onCopyCode(state.group) },
                onShareClick = { onShareCode(state.group) },
                onRenewClick = actions.onRenewClick,
            )
        }
        item {
            Text(
                text = stringResource(R.string.group_members_title, state.members.size),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = spacing.sm),
            )
        }
        items(state.members, key = { item -> item.member.userId }) { item -> MemberRow(item) }
        item { LeaveGroupButton(enabled, actions.onLeaveClick, Modifier.padding(top = spacing.lg)) }
    }
}

/** Shows a failure or a notice as a message at the bottom, and tells the ViewModel when it has been shown. */
@Composable
private fun SnackbarMessages(state: MyGroupUiState.Success?, actions: MyGroupActions, hostState: SnackbarHostState) {
    val failureText = state?.failure?.let { failure ->
        stringResource(if (failure == MyGroupFailure.Network) R.string.group_error_network else R.string.group_error_other)
    }
    val noticeText = state?.notice?.let { stringResource(R.string.group_code_copied) }
    LaunchedEffect(failureText) {
        if (failureText != null) {
            hostState.showSnackbar(failureText)
            actions.onFailureShown()
        }
    }
    LaunchedEffect(noticeText) {
        if (noticeText != null) {
            hostState.showSnackbar(noticeText)
            actions.onNoticeShown()
        }
    }
}
