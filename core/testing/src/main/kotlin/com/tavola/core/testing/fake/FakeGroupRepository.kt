package com.tavola.core.testing.fake

import com.tavola.domain.model.AuthUser
import com.tavola.domain.model.Group
import com.tavola.domain.model.GroupActionResult
import com.tavola.domain.model.GroupFailure
import com.tavola.domain.model.GroupMember
import com.tavola.domain.model.GroupRules
import com.tavola.domain.model.InviteCode
import com.tavola.domain.model.JoinGroupResult
import com.tavola.domain.model.MyGroupState
import com.tavola.domain.repository.GroupRepository
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory [GroupRepository] for tests, with the same behavior as the real one: one group per person, a code that finds the
 * group, and a renewed code that stops the old one working. It acts for [user] (`null` is nobody signed in). [failWith], when
 * set, makes the next actions fail with that reason; the codes come from [random], seeded so tests are repeatable.
 */
class FakeGroupRepository(private val user: AuthUser? = FakeAuthRepository.SampleUser, private val random: Random = Random(seed = 1)) :
    GroupRepository {
    private data class Stored(val group: Group, val members: List<GroupMember>)

    private data class State(val groups: Map<String, Stored> = emptyMap(), val groupOfUser: Map<String, String> = emptyMap())

    private val state = MutableStateFlow(State())
    private var nextGroupId = 0

    /** When set, every action fails with this reason instead of doing anything. */
    var failWith: GroupFailure? = null

    override val groupId: Flow<String?> = state.map { current -> user?.let { signedIn -> current.groupOfUser[signedIn.id] } }

    override val myGroup: Flow<MyGroupState> = state.map { current ->
        val stored = user?.let { signedIn -> current.groupOfUser[signedIn.id] }?.let { groupId -> current.groups[groupId] }
        if (stored == null) MyGroupState.None else MyGroupState.Joined(stored.group, stored.members)
    }

    /** A group that already exists, made by other people, for tests that join it. Returns it, with its code. */
    fun seedGroup(name: String, memberIds: List<String>, inviteCode: String = InviteCode.generate(random)): Group {
        val group = Group(id = "group-${nextGroupId++}", name = name, memberIds = memberIds, inviteCode = inviteCode)
        val members = memberIds.map { memberId -> GroupMember(memberId, displayName = memberId, photoUrl = null, colorIndex = 0) }
        state.update { current ->
            current.copy(
                groups = current.groups + (group.id to Stored(group, members)),
                groupOfUser = current.groupOfUser + memberIds.associateWith { group.id },
            )
        }
        return group
    }

    override suspend fun createGroup(name: String): GroupActionResult {
        val signedIn = user ?: return GroupActionResult.Failed(GroupFailure.NotSignedIn)
        failWith?.let { reason -> return GroupActionResult.Failed(reason) }
        val cleanName = GroupRules.cleanName(name) ?: return GroupActionResult.Failed(GroupFailure.InvalidName)
        if (state.value.groupOfUser.containsKey(signedIn.id)) return GroupActionResult.Failed(GroupFailure.AlreadyInGroup)

        val group = Group("group-${nextGroupId++}", cleanName, listOf(signedIn.id), newCode())
        state.update { current ->
            current.copy(
                groups = current.groups + (group.id to Stored(group, listOf(signedIn.asMember()))),
                groupOfUser = current.groupOfUser + (signedIn.id to group.id),
            )
        }
        return GroupActionResult.Done
    }

    override suspend fun joinGroup(code: String): JoinGroupResult {
        val parsed = InviteCode.parse(code) ?: return JoinGroupResult.InvalidCode
        val signedIn = user ?: return JoinGroupResult.Failed(GroupFailure.NotSignedIn)
        failWith?.let { reason -> return JoinGroupResult.Failed(reason) }
        if (state.value.groupOfUser.containsKey(signedIn.id)) return JoinGroupResult.Failed(GroupFailure.AlreadyInGroup)
        val stored = state.value.groups.values.firstOrNull { candidate -> candidate.group.inviteCode == parsed }
            ?: return JoinGroupResult.InvalidCode

        val joined = Stored(stored.group.copy(memberIds = stored.group.memberIds + signedIn.id), stored.members + signedIn.asMember())
        state.update { current ->
            current.copy(
                groups = current.groups + (stored.group.id to joined),
                groupOfUser = current.groupOfUser + (signedIn.id to stored.group.id),
            )
        }
        return JoinGroupResult.Joined
    }

    override suspend fun renameGroup(name: String): GroupActionResult {
        val cleanName = GroupRules.cleanName(name) ?: return GroupActionResult.Failed(GroupFailure.InvalidName)
        return changeMyGroup { stored -> stored.copy(group = stored.group.copy(name = cleanName)) }
    }

    override suspend fun renewInviteCode(): GroupActionResult = changeMyGroup { stored ->
        stored.copy(group = stored.group.copy(inviteCode = newCode()))
    }

    override suspend fun leaveGroup(): GroupActionResult {
        val signedIn = user ?: return GroupActionResult.Failed(GroupFailure.NotSignedIn)
        failWith?.let { reason -> return GroupActionResult.Failed(reason) }
        state.update { current ->
            val groupId = current.groupOfUser[signedIn.id] ?: return@update current
            val stored = current.groups.getValue(groupId)
            // The group stays as it is when the last one leaves: nothing is deleted.
            val left = Stored(
                group = stored.group.copy(memberIds = stored.group.memberIds - signedIn.id),
                members = stored.members.filterNot { member -> member.userId == signedIn.id },
            )
            current.copy(groups = current.groups + (groupId to left), groupOfUser = current.groupOfUser - signedIn.id)
        }
        return GroupActionResult.Done
    }

    /** Applies [change] to the group of the person and stores the result; fails if they have none. */
    private fun changeMyGroup(change: (Stored) -> Stored): GroupActionResult {
        val signedIn = user ?: return GroupActionResult.Failed(GroupFailure.NotSignedIn)
        failWith?.let { reason -> return GroupActionResult.Failed(reason) }
        val groupId = state.value.groupOfUser[signedIn.id] ?: return GroupActionResult.Failed(GroupFailure.Other)
        val changed = change(state.value.groups.getValue(groupId))
        state.update { current -> current.copy(groups = current.groups + (groupId to changed)) }
        return GroupActionResult.Done
    }

    private fun newCode(): String {
        val taken = state.value.groups.values.map { stored -> stored.group.inviteCode }.toSet()
        return generateSequence { InviteCode.generate(random) }.first { code -> code !in taken }
    }

    private fun AuthUser.asMember() = GroupMember(id, displayName, photoUrl, GroupRules.memberColorIndex(id))
}
