package com.tavola.feature.groups

import androidx.compose.runtime.Immutable
import com.tavola.domain.model.GroupRules
import com.tavola.domain.model.InviteCode

/** The three screens of the first step: choose what to do, name a new group, or type the code of an existing one. */
enum class SetupStep { Choose, Create, Join }

/** What went wrong, in the terms the person can act on. */
enum class SetupGroupError {
    /** No connection: they can try again when it is back. */
    Network,

    /** No group has that code: it was typed wrong or it was renewed. */
    InvalidCode,

    /** Anything else. */
    Other,
}

/**
 * @property name what has been typed for the new group (it is cleaned up when it is created).
 * @property code what has been typed for the code, in capitals and without dashes or spaces.
 * @property isWorking the group is being created or joined: the buttons wait.
 * @property error why the last attempt failed, or `null`.
 */
@Immutable
data class SetupGroupUiState(
    val step: SetupStep = SetupStep.Choose,
    val name: String = "",
    val code: String = "",
    val isWorking: Boolean = false,
    val error: SetupGroupError? = null,
) {
    val canCreate: Boolean get() = !isWorking && GroupRules.cleanName(name) != null

    val canJoin: Boolean get() = !isWorking && InviteCode.parse(code) != null
}
