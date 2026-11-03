package com.tavola.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.model.GroupActionResult
import com.tavola.domain.model.GroupFailure
import com.tavola.domain.model.GroupRules
import com.tavola.domain.model.InviteCode
import com.tavola.domain.model.JoinGroupResult
import com.tavola.domain.repository.GroupRepository
import com.tavola.domain.usecase.CreateGroupWithStarterRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The first step for someone with no group: create one (which starts with the example recipes) or join one with a code. When it works the group of the person changes
 * and the gate swaps this screen for the app, so there is nothing to report back; the state goes back to the start for the next
 * time someone has no group (for example after leaving one).
 */
@HiltViewModel
class SetupGroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val createGroupWithStarterRecipes: CreateGroupWithStarterRecipesUseCase,
) : ViewModel() {
    private val state = MutableStateFlow(SetupGroupUiState())
    val uiState: StateFlow<SetupGroupUiState> = state.asStateFlow()

    fun onCreateOptionClick() = goTo(SetupStep.Create)

    fun onJoinOptionClick() = goTo(SetupStep.Join)

    /** Back to the choice, unless something is being saved. */
    fun onBackClick() {
        if (!state.value.isWorking) goTo(SetupStep.Choose)
    }

    fun onNameChange(text: String) {
        state.update { current -> current.copy(name = text.take(GroupRules.MAX_NAME_LENGTH), error = null) }
    }

    fun onCodeChange(text: String) {
        val code = text.filter { character -> character.isLetterOrDigit() }.uppercase().take(InviteCode.LENGTH)
        state.update { current -> current.copy(code = code, error = null) }
    }

    fun onCreateClick() {
        val current = state.value
        if (!current.canCreate) return
        state.value = current.copy(isWorking = true, error = null)
        viewModelScope.launch {
            val result = createGroupWithStarterRecipes(current.name)
            finish(if (result is GroupActionResult.Failed) result.reason.toError() else null)
        }
    }

    fun onJoinClick() {
        val current = state.value
        if (!current.canJoin) return
        state.value = current.copy(isWorking = true, error = null)
        viewModelScope.launch {
            val error = when (val result = groupRepository.joinGroup(current.code)) {
                JoinGroupResult.Joined -> null
                JoinGroupResult.InvalidCode -> SetupGroupError.InvalidCode
                is JoinGroupResult.Failed -> result.reason.toError()
            }
            finish(error)
        }
    }

    private fun goTo(step: SetupStep) {
        state.update { current -> current.copy(step = step, error = null) }
    }

    private fun finish(error: SetupGroupError?) {
        state.value = if (error == null) SetupGroupUiState() else state.value.copy(isWorking = false, error = error)
    }

    private fun GroupFailure.toError(): SetupGroupError = if (this ==
        GroupFailure.Network
    ) {
        SetupGroupError.Network
    } else {
        SetupGroupError.Other
    }
}
