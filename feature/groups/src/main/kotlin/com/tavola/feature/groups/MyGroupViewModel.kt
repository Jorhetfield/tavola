package com.tavola.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.model.GroupActionResult
import com.tavola.domain.model.GroupFailure
import com.tavola.domain.model.MyGroupState
import com.tavola.domain.repository.AuthRepository
import com.tavola.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The "My group" screen: the name, the invite code, the members, and what a member can do (rename, renew the code, leave).
 * Copying and sharing the code need the Activity, so the screen does them and only reports the copy here. When the person
 * leaves, their group becomes none and the group gate swaps the whole app, so there is nothing to close from here.
 */
@HiltViewModel
class MyGroupViewModel @Inject constructor(private val groupRepository: GroupRepository, authRepository: AuthRepository) : ViewModel() {
    private val retryCount = MutableStateFlow(0)
    private val interaction = MutableStateFlow(Interaction())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val group: Flow<GroupLoad> = retryCount.flatMapLatest {
        groupRepository.myGroup.map<MyGroupState, GroupLoad> { state -> GroupLoad.Loaded(state) }.catch { emit(GroupLoad.Failed) }
    }

    val uiState: StateFlow<MyGroupUiState> = combine(group, authRepository.currentUser, interaction) { load, user, current ->
        when (load) {
            GroupLoad.Failed -> MyGroupUiState.Error

            is GroupLoad.Loaded -> when (val state = load.state) {
                MyGroupState.None -> MyGroupUiState.Loading

                is MyGroupState.Joined -> MyGroupUiState.Success(
                    group = state.group,
                    members = state.members.map { member -> MemberItem(member, isMe = member.userId == user?.id) },
                    dialog = current.dialog,
                    isWorking = current.isWorking,
                    failure = current.failure,
                    isCodeCopied = current.isCodeCopied,
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MyGroupUiState.Loading)

    fun onRenameClick() = openDialog(MyGroupDialog.Rename)

    fun onRenewClick() = openDialog(MyGroupDialog.RenewCode)

    fun onLeaveClick() = openDialog(MyGroupDialog.Leave)

    fun onDialogDismiss() {
        interaction.update { current -> current.copy(dialog = null) }
    }

    fun onRenameConfirm(name: String) = perform { groupRepository.renameGroup(name) }

    fun onRenewConfirm() = perform { groupRepository.renewInviteCode() }

    fun onLeaveConfirm() = perform { groupRepository.leaveGroup() }

    fun onCodeCopied() {
        interaction.update { current -> current.copy(isCodeCopied = true) }
    }

    fun onCodeCopiedShown() {
        interaction.update { current -> current.copy(isCodeCopied = false) }
    }

    fun onFailureShown() {
        interaction.update { current -> current.copy(failure = null) }
    }

    fun onRetryClick() {
        retryCount.update { count -> count + 1 }
    }

    private fun openDialog(dialog: MyGroupDialog) {
        interaction.update { current -> if (current.isWorking) current else current.copy(dialog = dialog) }
    }

    /** Closes the dialog, runs [action] while the buttons wait, and keeps why it failed (if it did) to show it. */
    private fun perform(action: suspend () -> GroupActionResult) {
        if (interaction.value.isWorking) return
        interaction.update { current -> current.copy(dialog = null, isWorking = true, failure = null) }
        viewModelScope.launch {
            val result = action()
            val failure = (result as? GroupActionResult.Failed)?.let { failed ->
                if (failed.reason == GroupFailure.Network) MyGroupFailure.Network else MyGroupFailure.Other
            }
            interaction.update { current -> current.copy(isWorking = false, failure = failure) }
        }
    }

    private data class Interaction(
        val dialog: MyGroupDialog? = null,
        val isWorking: Boolean = false,
        val failure: MyGroupFailure? = null,
        val isCodeCopied: Boolean = false,
    )

    private sealed interface GroupLoad {
        data class Loaded(val state: MyGroupState) : GroupLoad

        data object Failed : GroupLoad
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
