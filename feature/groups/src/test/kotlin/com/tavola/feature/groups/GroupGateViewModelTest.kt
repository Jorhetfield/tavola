package com.tavola.feature.groups

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.tavola.core.testing.fake.FakeGroupRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.MyGroupState
import com.tavola.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class GroupGateViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val repository = FakeGroupRepository()

    @Test
    fun `someone with no group sees the first step`() = runTest {
        GroupGateViewModel(repository).uiState.test {
            assertEquals(GroupGateUiState.NoGroup, awaitState(GroupGateUiState.NoGroup))
        }
    }

    @Test
    fun `someone with a group sees the app`() = runTest {
        repository.createGroup("Casa")

        GroupGateViewModel(repository).uiState.test {
            assertEquals(GroupGateUiState.HasGroup, awaitState(GroupGateUiState.HasGroup))
        }
    }

    @Test
    fun `creating and leaving a group swap the gate at once`() = runTest {
        GroupGateViewModel(repository).uiState.test {
            awaitState(GroupGateUiState.NoGroup)

            repository.createGroup("Casa")
            awaitState(GroupGateUiState.HasGroup)

            repository.leaveGroup()
            awaitState(GroupGateUiState.NoGroup)
        }
    }

    @Test
    fun `renaming the group does not change the gate`() = runTest {
        repository.createGroup("Casa")

        GroupGateViewModel(repository).uiState.test {
            awaitState(GroupGateUiState.HasGroup)

            repository.renameGroup("Piso")

            expectNoEvents()
        }
    }

    @Test
    fun `a repository that fails shows an error and retrying recovers`() = runTest {
        val flaky = FlakyGroupRepository(repository)
        val viewModel = GroupGateViewModel(flaky)

        viewModel.uiState.test {
            awaitState(GroupGateUiState.Error)

            flaky.shouldFail = false
            viewModel.onRetryClick()

            awaitState(GroupGateUiState.NoGroup)
        }
    }

    /** Skips the states before [expected]: the first one is always `Loading`. */
    private suspend fun ReceiveTurbine<GroupGateUiState>.awaitState(expected: GroupGateUiState): GroupGateUiState {
        while (true) {
            val item = awaitItem()
            if (item == expected) return item
        }
    }

    private class FlakyGroupRepository(private val delegate: GroupRepository) : GroupRepository by delegate {
        var shouldFail = true

        override val myGroup: Flow<MyGroupState>
            get() = if (shouldFail) flow { throw IllegalStateException("offline") } else delegate.myGroup
    }
}
