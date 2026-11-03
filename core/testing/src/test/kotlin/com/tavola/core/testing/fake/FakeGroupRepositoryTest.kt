package com.tavola.core.testing.fake

import app.cash.turbine.test
import com.tavola.domain.model.GroupActionResult
import com.tavola.domain.model.GroupFailure
import com.tavola.domain.model.InviteCode
import com.tavola.domain.model.JoinGroupResult
import com.tavola.domain.model.MyGroupState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class FakeGroupRepositoryTest {
    private val marta = FakeAuthRepository.SampleUser
    private val repository = FakeGroupRepository(marta)

    private suspend fun joined() = repository.myGroup.first() as MyGroupState.Joined

    @Test
    fun `a person with no group starts with none`() = runTest {
        assertEquals(MyGroupState.None, repository.myGroup.first())
    }

    @Test
    fun `creating a group makes the person its only member with a code`() = runTest {
        assertEquals(GroupActionResult.Done, repository.createGroup("  Casa  "))

        val group = joined()
        assertEquals("Casa", group.group.name)
        assertEquals(listOf(marta.id), group.group.memberIds)
        assertEquals(listOf(marta.id), group.members.map { member -> member.userId })
        assertEquals(group.group.inviteCode, InviteCode.parse(group.group.inviteCode))
    }

    @Test
    fun `a blank name does not create a group, and a second group is refused`() = runTest {
        assertEquals(GroupActionResult.Failed(GroupFailure.InvalidName), repository.createGroup("   "))
        assertEquals(MyGroupState.None, repository.myGroup.first())

        repository.createGroup("Casa")

        assertEquals(GroupActionResult.Failed(GroupFailure.AlreadyInGroup), repository.createGroup("Otra"))
    }

    @Test
    fun `joining with a code typed in lower case with a dash adds the person to that group`() = runTest {
        val target = repository.seedGroupOf(others = listOf("user-luis"), inviteCode = "K7M2QX4R")

        assertEquals(JoinGroupResult.Joined, repository.joinGroup("k7m2-qx4r"))

        val state = joined()
        assertEquals(target.id, state.group.id)
        assertEquals(listOf("user-luis", marta.id), state.group.memberIds)
        assertEquals(setOf("user-luis", marta.id), state.members.map { member -> member.userId }.toSet())
    }

    @Test
    fun `an unknown code or text that is not a code does not join`() = runTest {
        repository.seedGroupOf(others = listOf("user-luis"), inviteCode = "K7M2QX4R")

        assertEquals(JoinGroupResult.InvalidCode, repository.joinGroup("ZZZZZZZZ"))
        assertEquals(JoinGroupResult.InvalidCode, repository.joinGroup("hello"))
        assertEquals(MyGroupState.None, repository.myGroup.first())
    }

    @Test
    fun `a person who already has a group cannot join another`() = runTest {
        repository.createGroup("Casa")
        repository.seedGroupOf(others = listOf("user-luis"), inviteCode = "K7M2QX4R")

        assertEquals(JoinGroupResult.Failed(GroupFailure.AlreadyInGroup), repository.joinGroup("K7M2QX4R"))
    }

    @Test
    fun `any member can rename the group, and a blank name is refused`() = runTest {
        repository.createGroup("Casa")

        assertEquals(GroupActionResult.Done, repository.renameGroup("Piso de Madrid"))
        assertEquals("Piso de Madrid", joined().group.name)

        assertEquals(GroupActionResult.Failed(GroupFailure.InvalidName), repository.renameGroup("  "))
        assertEquals("Piso de Madrid", joined().group.name)
    }

    @Test
    fun `renewing the code gives a new valid one`() = runTest {
        repository.createGroup("Casa")
        val oldCode = joined().group.inviteCode

        assertEquals(GroupActionResult.Done, repository.renewInviteCode())

        val newCode = joined().group.inviteCode
        assertNotEquals(oldCode, newCode)
        assertEquals(newCode, InviteCode.parse(newCode))
    }

    @Test
    fun `renaming or renewing without a group fails`() = runTest {
        assertEquals(GroupActionResult.Failed(GroupFailure.Other), repository.renameGroup("Casa"))
        assertEquals(GroupActionResult.Failed(GroupFailure.Other), repository.renewInviteCode())
    }

    @Test
    fun `leaving takes the person out but the group stays, so its code still joins it`() = runTest {
        repository.seedGroupOf(others = listOf("user-luis"), inviteCode = "K7M2QX4R", withPerson = true)
        assertEquals(listOf("user-luis", marta.id), joined().group.memberIds)

        assertEquals(GroupActionResult.Done, repository.leaveGroup())
        assertEquals(MyGroupState.None, repository.myGroup.first())

        // Nothing was deleted: the code finds the group again, with the person who stayed.
        assertEquals(JoinGroupResult.Joined, repository.joinGroup("K7M2QX4R"))
        assertEquals(listOf("user-luis", marta.id), joined().group.memberIds)
    }

    @Test
    fun `the last member can leave and then create another group`() = runTest {
        repository.createGroup("Casa")

        assertEquals(GroupActionResult.Done, repository.leaveGroup())

        assertEquals(MyGroupState.None, repository.myGroup.first())
        assertEquals(GroupActionResult.Done, repository.createGroup("Otra casa"))
    }

    @Test
    fun `with nobody signed in every action says so`() = runTest {
        val signedOut = FakeGroupRepository(user = null)

        assertEquals(GroupActionResult.Failed(GroupFailure.NotSignedIn), signedOut.createGroup("Casa"))
        assertEquals(JoinGroupResult.Failed(GroupFailure.NotSignedIn), signedOut.joinGroup("K7M2QX4R"))
        assertEquals(GroupActionResult.Failed(GroupFailure.NotSignedIn), signedOut.leaveGroup())
        assertEquals(MyGroupState.None, signedOut.myGroup.first())
    }

    @Test
    fun `a failure set on the fake is what the actions report and changes nothing`() = runTest {
        repository.failWith = GroupFailure.Network

        assertEquals(GroupActionResult.Failed(GroupFailure.Network), repository.createGroup("Casa"))
        assertEquals(MyGroupState.None, repository.myGroup.first())
    }

    @Test
    fun `the group is followed as it changes`() = runTest {
        repository.myGroup.test {
            assertEquals(MyGroupState.None, awaitItem())

            repository.createGroup("Casa")
            assertEquals("Casa", (awaitItem() as MyGroupState.Joined).group.name)

            repository.renameGroup("Piso")
            assertEquals("Piso", (awaitItem() as MyGroupState.Joined).group.name)

            repository.leaveGroup()
            assertEquals(MyGroupState.None, awaitItem())
        }
    }

    /** A group of [others] with [inviteCode]; the person is in it only with [withPerson]. */
    private fun FakeGroupRepository.seedGroupOf(others: List<String>, inviteCode: String, withPerson: Boolean = false) =
        seedGroup("Casa de Luis", memberIds = if (withPerson) others + marta.id else others, inviteCode = inviteCode)
}
