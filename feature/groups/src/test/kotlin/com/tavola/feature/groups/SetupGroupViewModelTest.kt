package com.tavola.feature.groups

import com.tavola.core.testing.fake.FakeGroupRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.GroupFailure
import com.tavola.domain.model.GroupRules
import com.tavola.domain.model.MyGroupState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SetupGroupViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val repository = FakeGroupRepository()
    private val viewModel = SetupGroupViewModel(repository)

    private val state get() = viewModel.uiState.value

    @Test
    fun `starts on the choice with nothing typed`() {
        assertEquals(SetupGroupUiState(), state)
    }

    @Test
    fun `the options lead to their step and back leads to the choice`() {
        viewModel.onCreateOptionClick()
        assertEquals(SetupStep.Create, state.step)

        viewModel.onBackClick()
        assertEquals(SetupStep.Choose, state.step)

        viewModel.onJoinOptionClick()
        assertEquals(SetupStep.Join, state.step)
    }

    @Test
    fun `a name is not created until it has something and is cut at the limit`() {
        viewModel.onCreateOptionClick()
        assertFalse(state.canCreate)

        viewModel.onNameChange("   ")
        assertFalse(state.canCreate)

        viewModel.onNameChange("Casa")
        assertTrue(state.canCreate)

        viewModel.onNameChange("a".repeat(GroupRules.MAX_NAME_LENGTH + 10))
        assertEquals(GroupRules.MAX_NAME_LENGTH, state.name.length)
    }

    @Test
    fun `the code is kept in capitals without dashes or spaces and at most eight characters`() {
        viewModel.onJoinOptionClick()

        viewModel.onCodeChange("k7m2-qx4r extra")

        assertEquals("K7M2QX4R", state.code)
        assertTrue(state.canJoin)
        viewModel.onCodeChange("K7M2")
        assertFalse(state.canJoin)
    }

    @Test
    fun `creating a group makes the person a member and resets the step`() = runTest {
        viewModel.onCreateOptionClick()
        viewModel.onNameChange("Casa")

        viewModel.onCreateClick()

        assertTrue(repository.myGroup.first() is MyGroupState.Joined)
        assertEquals(SetupGroupUiState(), state)
    }

    @Test
    fun `joining with a code that exists makes the person a member`() = runTest {
        repository.seedGroup("Casa de Luis", memberIds = listOf("user-luis"), inviteCode = "K7M2QX4R")
        viewModel.onJoinOptionClick()
        viewModel.onCodeChange("k7m2-qx4r")

        viewModel.onJoinClick()

        assertTrue(repository.myGroup.first() is MyGroupState.Joined)
        assertEquals(SetupGroupUiState(), state)
    }

    @Test
    fun `a code that no group has says it is not valid and keeps what was typed`() = runTest {
        viewModel.onJoinOptionClick()
        viewModel.onCodeChange("ZZZZZZZZ")

        viewModel.onJoinClick()

        assertEquals(SetupGroupError.InvalidCode, state.error)
        assertEquals("ZZZZZZZZ", state.code)
        assertFalse(state.isWorking)
        assertEquals(MyGroupState.None, repository.myGroup.first())
    }

    @Test
    fun `a lost connection and any other failure are told apart, and typing clears the error`() = runTest {
        viewModel.onCreateOptionClick()
        viewModel.onNameChange("Casa")

        repository.failWith = GroupFailure.Network
        viewModel.onCreateClick()
        assertEquals(SetupGroupError.Network, state.error)

        repository.failWith = GroupFailure.Other
        viewModel.onCreateClick()
        assertEquals(SetupGroupError.Other, state.error)

        viewModel.onNameChange("Casa 2")
        assertEquals(null, state.error)
    }

    @Test
    fun `nothing is asked of the server while the form is not valid`() = runTest {
        viewModel.onCreateOptionClick()
        viewModel.onCreateClick()
        viewModel.onJoinOptionClick()
        viewModel.onJoinClick()

        assertEquals(MyGroupState.None, repository.myGroup.first())
        assertEquals(null, state.error)
    }
}
