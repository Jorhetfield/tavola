package com.tavola.feature.groups

/** Everything the first step can ask of its ViewModel. Does nothing by default (previews and screenshots). */
data class SetupGroupActions(
    val onCreateOptionClick: () -> Unit = {},
    val onJoinOptionClick: () -> Unit = {},
    val onBackClick: () -> Unit = {},
    val onNameChange: (text: String) -> Unit = {},
    val onCodeChange: (text: String) -> Unit = {},
    val onCreateClick: () -> Unit = {},
    val onJoinClick: () -> Unit = {},
)

/**
 * The actions of the screen wired to this ViewModel. Every action that the screen can ask for has to be here: the defaults of
 * [SetupGroupActions] do nothing, so one left out would compile and simply not work (`SetupGroupActionsTest` checks it).
 */
internal fun SetupGroupViewModel.toActions(): SetupGroupActions = SetupGroupActions(
    onCreateOptionClick = ::onCreateOptionClick,
    onJoinOptionClick = ::onJoinOptionClick,
    onBackClick = ::onBackClick,
    onNameChange = ::onNameChange,
    onCodeChange = ::onCodeChange,
    onCreateClick = ::onCreateClick,
    onJoinClick = ::onJoinClick,
)
