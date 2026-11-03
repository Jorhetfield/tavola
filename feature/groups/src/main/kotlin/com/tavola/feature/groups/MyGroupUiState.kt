package com.tavola.feature.groups

import androidx.compose.runtime.Immutable
import com.tavola.domain.model.Group
import com.tavola.domain.model.GroupMember

/** A person of the group, and whether it is the one using the app. */
@Immutable
data class MemberItem(val member: GroupMember, val isMe: Boolean)

/** The dialog that is open on the screen, if any. */
enum class MyGroupDialog { Rename, RenewCode, Leave }

/** Why the last action did not work, shown as a message. */
enum class MyGroupFailure { Network, Other }

/** Something that went well and is worth a message. */
enum class MyGroupNotice { CodeCopied, }

sealed interface MyGroupUiState {
    data object Loading : MyGroupUiState

    data object Error : MyGroupUiState

    /**
     * @property dialog the dialog that is open, or `null`.
     * @property isWorking an action is being saved: the buttons wait.
     * @property failure why the last action failed, until the message is shown.
     * @property notice what went well, until the message is shown.
     */
    @Immutable
    data class Success(
        val group: Group,
        val members: List<MemberItem>,
        val dialog: MyGroupDialog? = null,
        val isWorking: Boolean = false,
        val failure: MyGroupFailure? = null,
        val notice: MyGroupNotice? = null,
    ) : MyGroupUiState
}
