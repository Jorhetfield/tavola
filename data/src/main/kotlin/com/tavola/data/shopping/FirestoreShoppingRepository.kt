package com.tavola.data.shopping

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.tavola.data.firestore.snapshotFlow
import com.tavola.data.group.GroupPaths
import com.tavola.domain.model.ShoppingItem
import com.tavola.domain.repository.AuthRepository
import com.tavola.domain.repository.GroupRepository
import com.tavola.domain.repository.ShoppingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The shopping list of the group in Firestore: every member reads and writes the same items, and a change shows up on every phone
 * through the listeners. With no group there is no list.
 *
 * Writes are kept by Firestore when there is no connection and sent later, so they only wait a short while for the server's
 * answer instead of hanging while offline. A refusal (an item that was removed meanwhile, say) is not an error for the caller: the
 * listeners show what the server has.
 */
@Singleton
internal class FirestoreShoppingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
) : ShoppingRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeItems(): Flow<List<ShoppingItem>> = groupRepository.groupId.flatMapLatest { groupId ->
        if (groupId == null) {
            flowOf(emptyList())
        } else {
            items(groupId).snapshotFlow().map { snapshot ->
                snapshot.documents
                    .mapNotNull { document -> document.data?.toShoppingItem(document.id) }
                    .sortedWith(compareBy({ item -> item.createdAt }, { item -> item.id }))
            }
        }
    }

    override suspend fun saveItem(item: ShoppingItem): String {
        val collection = items(currentGroupId())
        val isNew = item.id.isBlank()
        val reference = if (isNew) collection.document() else collection.document(item.id)
        // Only a new item is stamped and signed; a restored one keeps its time, and the merge leaves whoever added it as they were.
        val stamped = if (isNew) item.copy(createdAt = System.currentTimeMillis()) else item
        val addedBy = if (isNew) authRepository.currentUser.first()?.id else item.addedBy
        write { reference.set(stamped.toDocument(addedBy), SetOptions.merge()).await() }
        return reference.id
    }

    override suspend fun setChecked(id: String, checked: Boolean) {
        val groupId = groupRepository.groupId.first() ?: return
        write { items(groupId).document(id).update(ShoppingFields.CHECKED, checked).await() }
    }

    override suspend fun removeItems(ids: List<String>) {
        if (ids.isEmpty()) return
        val groupId = groupRepository.groupId.first() ?: return
        write {
            val collection = items(groupId)
            firestore.batch().apply { ids.forEach { id -> delete(collection.document(id)) } }.commit().await()
        }
    }

    private suspend fun write(change: suspend () -> Unit) {
        try {
            withTimeoutOrNull(ACKNOWLEDGEMENT_WAIT_MILLIS) { change() }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (refused: FirebaseFirestoreException) {
            // The list goes back to what the server has.
        }
    }

    /** The group of the person; right after a group is created it can take a moment to arrive, so this waits a little for it. */
    private suspend fun currentGroupId(): String = withTimeoutOrNull(GROUP_WAIT_MILLIS) { groupRepository.groupId.filterNotNull().first() }
        ?: error("There is no group to save the item in")

    private fun items(groupId: String): CollectionReference =
        firestore.collection(GroupPaths.GROUPS).document(groupId).collection(SHOPPING_COLLECTION)

    private companion object {
        const val ACKNOWLEDGEMENT_WAIT_MILLIS = 4_000L
        const val GROUP_WAIT_MILLIS = 10_000L
    }
}
