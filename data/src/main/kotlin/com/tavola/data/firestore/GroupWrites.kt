package com.tavola.data.firestore

import com.google.firebase.firestore.FirebaseFirestoreException
import com.tavola.domain.repository.GroupRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * What every Firestore repository that writes into the group of the person needs: the id of that group, and a way to send a change
 * without hanging while offline.
 *
 * Firestore keeps a write made with no connection and sends it later, so its task only completes when the server answers. A write
 * therefore waits a short while for the answer (a refusal comes within it) and then lets the listeners show the result.
 */
@Singleton
internal class GroupWrites @Inject constructor(private val groupRepository: GroupRepository) {
    /**
     * The group of the person, or `null` if they have none. Right after a group is created or joined it can take a moment to
     * arrive, so this waits a little for it instead of answering `null` at once.
     */
    suspend fun awaitGroupId(): String? = withTimeoutOrNull(GROUP_WAIT_MILLIS) { groupRepository.groupId.filterNotNull().first() }

    /** Like [awaitGroupId] but for a caller that cannot go on without a group: [what] is what it was going to save. */
    suspend fun requireGroupId(what: String): String = awaitGroupId() ?: error("There is no group to save the $what in")

    /** Waits a short while for [change] (a Firestore task) to be acknowledged; a refusal of the server is thrown at the caller. */
    suspend fun <T> acknowledged(change: suspend () -> T): T? = withTimeoutOrNull(ACKNOWLEDGEMENT_WAIT_MILLIS) { change() }

    /**
     * Runs [change] in the group of the person for a fire-and-forget caller. With no group there is nothing to change, and a
     * refusal of the server (an item that was removed meanwhile, say) is not an error for the caller: the listeners show what the
     * server has. Anything the repository can check before writing must be checked before, since a refusal is invisible here.
     */
    suspend fun write(change: suspend (groupId: String) -> Unit) {
        val groupId = awaitGroupId() ?: return
        try {
            acknowledged { change(groupId) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (refused: FirebaseFirestoreException) {
            // The screen goes back to what the server has.
        }
    }

    companion object {
        const val ACKNOWLEDGEMENT_WAIT_MILLIS = 4_000L
        const val GROUP_WAIT_MILLIS = 10_000L
    }
}
