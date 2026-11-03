package com.tavola.feature.groups

/** Whether the signed-in person has a group; the gate shows the app, the first step or a brief wait. */
sealed interface GroupGateUiState {
    data object Loading : GroupGateUiState

    /** The person belongs to no group yet: they have to create one or join one before they can use the app. */
    data object NoGroup : GroupGateUiState

    data object HasGroup : GroupGateUiState

    /** The group of the person could not be loaded. */
    data object Error : GroupGateUiState
}
