package com.tavola.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.model.MyGroupState
import com.tavola.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Whether the signed-in person has a group. It follows the repository, so creating, joining or leaving a group shows up at once. */
@HiltViewModel
class GroupGateViewModel @Inject constructor(groupRepository: GroupRepository) : ViewModel() {
    private val retryCount = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GroupGateUiState> = retryCount.flatMapLatest { attempt ->
        groupRepository.myGroup
            .map<MyGroupState, GroupGateUiState> { group ->
                if (group is MyGroupState.Joined) GroupGateUiState.HasGroup else GroupGateUiState.NoGroup
            }
            .catch { emit(GroupGateUiState.Error) }
            // Loading is only for a retry. When the collection restarts after the app was in the background the last state stands
            // until the new one arrives; showing Loading would swap the whole app for the loading screen on every return.
            .onStart { if (attempt > 0) emit(GroupGateUiState.Loading) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), GroupGateUiState.Loading)

    fun onRetryClick() {
        retryCount.update { count -> count + 1 }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
