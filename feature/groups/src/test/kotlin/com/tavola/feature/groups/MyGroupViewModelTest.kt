package com.tavola.feature.groups

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.tavola.core.testing.fake.FakeAuthRepository
import com.tavola.core.testing.fake.FakeGroupRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.GroupFailure
import com.tavola.domain.model.MyGroupState
import com.tavola.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class MyGroupViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val marta = FakeAuthRepository.SampleUser
    private val repository = FakeGroupRepository(marta)

    private fun viewModel(groups: GroupRepository = repository) = MyGroupViewModel(groups, FakeAuthRepository(initialUser = marta))

    private suspend fun joinedGroup() = repository.seedGroup("Casa", memberIds = listOf("user-luis", marta.id), inviteCode = "K7M2QX4R")

    @Test
    fun `with no group the screen is loading, as it is about to go away`() = runTest {
        viewModel().uiState.test {
            assertEquals(MyGroupUiState.Loading, expectMostRecentItem())
        }
    }

    @Test
    fun `the group is shown with its members and the one using the app marked`() = runTest {
        joinedGroup()

        viewModel().uiState.test {
            val state = awaitSuccess()

            assertEquals("Casa", state.group.name)
            assertEquals("K7M2QX4R", state.group.inviteCode)
            assertEquals(listOf("user-luis" to false, marta.id to true), state.members.map { item -> item.member.userId to item.isMe })
            assertNull(state.dialog)
            assertFalse(state.isWorking)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `each action opens its dialog and dismissing closes it`() = runTest {
        joinedGroup()
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onRenameClick()
            assertEquals(MyGroupDialog.Rename, awaitSuccess { state -> state.dialog != null }.dialog)
            viewModel.onRenewClick()
            assertEquals(MyGroupDialog.RenewCode, awaitSuccess { state -> state.dialog == MyGroupDialog.RenewCode }.dialog)
            viewModel.onLeaveClick()
            assertEquals(MyGroupDialog.Leave, awaitSuccess { state -> state.dialog == MyGroupDialog.Leave }.dialog)

            viewModel.onDialogDismiss()
            assertNull(awaitSuccess { state -> state.dialog == null }.dialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `renaming closes the dialog and changes the name`() = runTest {
        joinedGroup()
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onRenameClick()
            awaitSuccess { state -> state.dialog == MyGroupDialog.Rename }

            viewModel.onRenameConfirm("Piso de Madrid")

            val state = awaitSuccess { current -> current.group.name == "Piso de Madrid" }
            assertNull(state.dialog)
            assertFalse(state.isWorking)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `renewing the code gives the group a new one`() = runTest {
        joinedGroup()
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onRenewConfirm()

            val state = awaitSuccess { current -> current.group.inviteCode != "K7M2QX4R" }
            assertNotEquals("K7M2QX4R", state.group.inviteCode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `leaving takes the person out of the group`() = runTest {
        joinedGroup()
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onLeaveConfirm()

            assertEquals(MyGroupUiState.Loading, awaitUntil { state -> state == MyGroupUiState.Loading })
            assertEquals(MyGroupState.None, repositoryState())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failure is kept until the message has been shown`() = runTest {
        joinedGroup()
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitSuccess()
            repository.failWith = GroupFailure.Network

            viewModel.onRenewConfirm()
            assertEquals(MyGroupFailure.Network, awaitSuccess { state -> state.failure != null }.failure)

            viewModel.onFailureShown()
            assertNull(awaitSuccess { state -> state.failure == null }.failure)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a copied code shows a notice until it has been shown`() = runTest {
        joinedGroup()
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onCodeCopied()
            assertEquals(MyGroupNotice.CodeCopied, awaitSuccess { state -> state.notice != null }.notice)

            viewModel.onNoticeShown()
            assertNull(awaitSuccess { state -> state.notice == null }.notice)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a repository that fails shows an error and retrying recovers`() = runTest {
        joinedGroup()
        val flaky = FlakyGroupRepository(repository)
        val viewModel = viewModel(flaky)

        viewModel.uiState.test {
            assertEquals(MyGroupUiState.Error, awaitUntil { state -> state == MyGroupUiState.Error })

            flaky.shouldFail = false
            viewModel.onRetryClick()

            assertTrue(awaitUntil { state -> state is MyGroupUiState.Success } is MyGroupUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun repositoryState(): MyGroupState = repository.myGroup.first()

    private suspend fun ReceiveTurbine<MyGroupUiState>.awaitUntil(predicate: (MyGroupUiState) -> Boolean): MyGroupUiState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    private suspend fun ReceiveTurbine<MyGroupUiState>.awaitSuccess(
        predicate: (MyGroupUiState.Success) -> Boolean = { true },
    ): MyGroupUiState.Success {
        while (true) {
            val item = awaitItem()
            if (item is MyGroupUiState.Success && predicate(item)) return item
        }
    }

    private class FlakyGroupRepository(private val delegate: GroupRepository) : GroupRepository by delegate {
        var shouldFail = true

        override val myGroup: Flow<MyGroupState>
            get() = if (shouldFail) flow { throw IllegalStateException("offline") } else delegate.myGroup
    }
}
