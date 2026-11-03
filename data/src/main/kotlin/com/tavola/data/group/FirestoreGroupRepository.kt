package com.tavola.data.group

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.snapshots
import com.tavola.domain.model.AuthUser
import com.tavola.domain.model.GroupActionResult
import com.tavola.domain.model.GroupFailure
import com.tavola.domain.model.GroupRules
import com.tavola.domain.model.InviteCode
import com.tavola.domain.model.JoinGroupResult
import com.tavola.domain.model.MyGroupState
import com.tavola.domain.repository.AuthRepository
import com.tavola.domain.repository.GroupRepository
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.asKotlinRandom
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Groups in Firestore (see `GroupDocuments.kt` for how they are stored). Every change that touches more than one document goes
 * in a transaction, so a person is never in a group that does not list them, or the other way round. The rules in
 * `firestore.rules` are what keeps strangers out; this class only has to ask for things the rules allow.
 */
@Singleton
internal class FirestoreGroupRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
) : GroupRepository {
    // The invite code is the only thing that keeps strangers out of a group, so it comes from a secure source.
    private val random = SecureRandom().asKotlinRandom()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val myGroup: Flow<MyGroupState> = authRepository.currentUser.flatMapLatest { user ->
        if (user == null) flowOf(MyGroupState.None) else observeGroupOf(user.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeGroupOf(userId: String): Flow<MyGroupState> = userRef(userId).snapshots()
        .map { snapshot -> snapshot.getString(GroupFields.GROUP_ID) }
        .distinctUntilChanged()
        .flatMapLatest { groupId -> if (groupId == null) flowOf(MyGroupState.None) else observeGroup(groupId) }

    private fun observeGroup(groupId: String): Flow<MyGroupState> {
        val groupRef = groupRef(groupId)
        return combine(groupRef.snapshots(), groupRef.collection(GroupPaths.MEMBERS).snapshots()) { groupSnapshot, membersSnapshot ->
            val group = groupSnapshot.data?.toGroup(groupSnapshot.id)
            if (group == null) {
                MyGroupState.None
            } else {
                val members = membersSnapshot.documents
                    .mapNotNull { document -> document.data?.toGroupMember(document.id) }
                    .sortedBy { member -> member.displayName.orEmpty().lowercase() }
                MyGroupState.Joined(group, members)
            }
        }.catch { error ->
            // Leaving a group makes it unreadable a moment before the person's own document says they have no group.
            if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                emit(MyGroupState.None)
            } else {
                throw error
            }
        }
    }

    override suspend fun createGroup(name: String): GroupActionResult = groupAction { user ->
        val cleanName = GroupRules.cleanName(name) ?: return@groupAction GroupActionResult.Failed(GroupFailure.InvalidName)
        val groupRef = firestore.collection(GroupPaths.GROUPS).document()
        repeat(MAX_CODE_ATTEMPTS) {
            val code = InviteCode.generate(random)
            val outcome = firestore.runTransaction { transaction ->
                when {
                    transaction.hasGroup(user.id) -> CreateOutcome.AlreadyInGroup

                    transaction.get(inviteRef(code)).exists() -> CreateOutcome.CodeTaken

                    else -> {
                        transaction.set(groupRef, newGroupDocument(cleanName, user.id, code))
                        transaction.set(memberRef(groupRef.id, user.id), memberDocument(user, joinCode = null))
                        transaction.set(inviteRef(code), inviteDocument(groupRef.id))
                        transaction.set(userRef(user.id), mapOf(GroupFields.GROUP_ID to groupRef.id))
                        CreateOutcome.Created
                    }
                }
            }.await()
            when (outcome) {
                CreateOutcome.Created -> return@groupAction GroupActionResult.Done
                CreateOutcome.AlreadyInGroup -> return@groupAction GroupActionResult.Failed(GroupFailure.AlreadyInGroup)
                CreateOutcome.CodeTaken -> Unit
            }
        }
        GroupActionResult.Failed(GroupFailure.Other)
    }

    override suspend fun joinGroup(code: String): JoinGroupResult {
        val parsed = InviteCode.parse(code) ?: return JoinGroupResult.InvalidCode
        val user = authRepository.currentUser.first() ?: return JoinGroupResult.Failed(GroupFailure.NotSignedIn)
        return try {
            val outcome = firestore.runTransaction { transaction ->
                if (transaction.hasGroup(user.id)) return@runTransaction JoinOutcome.AlreadyInGroup
                val groupId =
                    transaction.get(inviteRef(parsed)).getString(GroupFields.GROUP_ID) ?: return@runTransaction JoinOutcome.InvalidCode
                // The member document carries the code, which is how the rules know this person was given it.
                transaction.set(memberRef(groupId, user.id), memberDocument(user, joinCode = parsed))
                transaction.update(groupRef(groupId), GroupFields.MEMBER_IDS, FieldValue.arrayUnion(user.id))
                transaction.set(userRef(user.id), mapOf(GroupFields.GROUP_ID to groupId))
                JoinOutcome.Joined
            }.await()
            when (outcome) {
                JoinOutcome.Joined -> JoinGroupResult.Joined
                JoinOutcome.InvalidCode -> JoinGroupResult.InvalidCode
                JoinOutcome.AlreadyInGroup -> JoinGroupResult.Failed(GroupFailure.AlreadyInGroup)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            // A code renewed a moment ago is refused by the rules because its invite is gone, and that is a code that is not valid.
            // A refusal while the invite is still there is not about the code (for example the rules are not published).
            if (failure.isPermissionDenied() && !inviteExists(parsed)) {
                JoinGroupResult.InvalidCode
            } else {
                JoinGroupResult.Failed(failure.toGroupFailure())
            }
        }
    }

    /** Whether the invite of [code] is there; if that cannot be told, it is taken to be there, so the refusal is not blamed on the code. */
    private suspend fun inviteExists(code: String): Boolean = try {
        inviteRef(code).get().await().exists()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        true
    }

    override suspend fun renameGroup(name: String): GroupActionResult = groupAction { user ->
        val cleanName = GroupRules.cleanName(name) ?: return@groupAction GroupActionResult.Failed(GroupFailure.InvalidName)
        val groupId = userRef(user.id).get().await().getString(GroupFields.GROUP_ID)
        if (groupId == null) {
            GroupActionResult.Failed(GroupFailure.Other)
        } else {
            // A write only completes once the server has it. With no connection it is kept and sent later, and the screen already
            // shows it, so after a short wait for the answer (a refusal comes within it) the rename counts as done.
            withTimeoutOrNull(ACKNOWLEDGEMENT_WAIT_MILLIS) { groupRef(groupId).update(GroupFields.NAME, cleanName).await() }
            GroupActionResult.Done
        }
    }

    override suspend fun renewInviteCode(): GroupActionResult = groupAction { user ->
        repeat(MAX_CODE_ATTEMPTS) {
            val newCode = InviteCode.generate(random)
            val renewed = firestore.runTransaction { transaction ->
                val groupId =
                    transaction.get(userRef(user.id)).getString(GroupFields.GROUP_ID) ?: return@runTransaction RenewOutcome.NoGroup
                val oldCode =
                    transaction.get(groupRef(groupId)).getString(GroupFields.INVITE_CODE) ?: return@runTransaction RenewOutcome.NoGroup
                if (transaction.get(inviteRef(newCode)).exists()) return@runTransaction RenewOutcome.CodeTaken
                transaction.update(groupRef(groupId), GroupFields.INVITE_CODE, newCode)
                transaction.delete(inviteRef(oldCode))
                transaction.set(inviteRef(newCode), inviteDocument(groupId))
                RenewOutcome.Renewed
            }.await()
            when (renewed) {
                RenewOutcome.Renewed -> return@groupAction GroupActionResult.Done
                RenewOutcome.NoGroup -> return@groupAction GroupActionResult.Failed(GroupFailure.Other)
                RenewOutcome.CodeTaken -> Unit
            }
        }
        GroupActionResult.Failed(GroupFailure.Other)
    }

    override suspend fun leaveGroup(): GroupActionResult = groupAction { user ->
        firestore.runTransaction { transaction ->
            val groupId = transaction.get(userRef(user.id)).getString(GroupFields.GROUP_ID)
            if (groupId != null) {
                transaction.update(groupRef(groupId), GroupFields.MEMBER_IDS, FieldValue.arrayRemove(user.id))
                transaction.delete(memberRef(groupId, user.id))
            }
            transaction.set(userRef(user.id), mapOf(GroupFields.GROUP_ID to null))
        }.await()
        GroupActionResult.Done
    }

    /** Runs [block] for the signed-in person and turns the failures of the server into a [GroupActionResult]. */
    private suspend fun groupAction(block: suspend (AuthUser) -> GroupActionResult): GroupActionResult {
        val user = authRepository.currentUser.first() ?: return GroupActionResult.Failed(GroupFailure.NotSignedIn)
        return try {
            block(user)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            GroupActionResult.Failed(failure.toGroupFailure())
        }
    }

    private fun Transaction.hasGroup(userId: String): Boolean = get(userRef(userId)).getString(GroupFields.GROUP_ID) != null

    private fun userRef(userId: String): DocumentReference = firestore.collection(GroupPaths.USERS).document(userId)

    private fun groupRef(groupId: String): DocumentReference = firestore.collection(GroupPaths.GROUPS).document(groupId)

    private fun memberRef(groupId: String, userId: String): DocumentReference =
        groupRef(groupId).collection(GroupPaths.MEMBERS).document(userId)

    private fun inviteRef(code: String): DocumentReference = firestore.collection(GroupPaths.INVITES).document(code)

    private enum class CreateOutcome { Created, AlreadyInGroup, CodeTaken }

    private enum class JoinOutcome { Joined, InvalidCode, AlreadyInGroup }

    private enum class RenewOutcome { Renewed, NoGroup, CodeTaken }

    private companion object {
        /** A new code can only clash with an existing one by a one-in-a-trillion chance; a few attempts are plenty. */
        const val MAX_CODE_ATTEMPTS = 3

        /** How long a single write waits for the server before it is left to finish on its own. */
        const val ACKNOWLEDGEMENT_WAIT_MILLIS = 4_000L
    }
}

/** A lost connection is the failure the person can fix; the rest is reported as a generic one. */
internal fun Throwable.toGroupFailure(): GroupFailure = when {
    this is FirebaseNetworkException -> GroupFailure.Network
    this is FirebaseFirestoreException && code in NETWORK_CODES -> GroupFailure.Network
    else -> GroupFailure.Other
}

internal fun Throwable.isPermissionDenied(): Boolean =
    this is FirebaseFirestoreException && code == FirebaseFirestoreException.Code.PERMISSION_DENIED

private val NETWORK_CODES = setOf(FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED)
