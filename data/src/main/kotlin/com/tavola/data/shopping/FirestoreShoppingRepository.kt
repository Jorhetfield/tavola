package com.tavola.data.shopping

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tavola.data.firestore.GroupWrites
import com.tavola.data.firestore.snapshotFlow
import com.tavola.data.group.GroupPaths
import com.tavola.domain.model.ShoppingItem
import com.tavola.domain.repository.AuthRepository
import com.tavola.domain.repository.GroupRepository
import com.tavola.domain.repository.ShoppingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * The shopping list of the group in Firestore: every member reads and writes the same items, and a change shows up on every phone
 * through the listeners. With no group there is no list.
 *
 * Writes go through [GroupWrites]: they wait for the group and for the server's answer a short while, and a refusal is not thrown
 * at the caller. That is why [saveItem] cleans the item first (see [ShoppingItem.normalized]) instead of leaving the rules to
 * refuse it.
 */
@Singleton
internal class FirestoreShoppingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    private val groupWrites: GroupWrites,
) : ShoppingRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeItems(): Flow<List<ShoppingItem>> = groupRepository.groupId.flatMapLatest { groupId ->
        if (groupId == null) {
            flowOf(emptyList())
        } else {
            items(groupId).snapshotFlow().map { snapshot ->
                snapshot.documents
                    .mapNotNull { document -> document.estimatedData()?.toShoppingItem(document.id) }
                    .sortedWith(compareBy({ item -> item.createdAt }, { item -> item.id }))
            }
        }
    }

    override suspend fun saveItem(item: ShoppingItem): String {
        val clean = item.normalized()
        val collection = items(groupWrites.requireGroupId("item"))
        val isNew = clean.id.isBlank()
        val reference = if (isNew) collection.document() else collection.document(clean.id)
        // Only a new item is stamped (with the server's time) and signed; a restored one keeps its time, and the merge leaves
        // whoever added it as they were.
        val document = if (isNew) {
            clean.toDocument(authRepository.currentUser.first()?.id, createdAtValue = FieldValue.serverTimestamp())
        } else {
            clean.toDocument(clean.addedBy)
        }
        groupWrites.write { reference.set(document, SetOptions.merge()).await() }
        return reference.id
    }

    override suspend fun setChecked(id: String, checked: Boolean) = groupWrites.write { groupId ->
        items(groupId).document(id).update(ShoppingFields.CHECKED, checked).await()
    }

    override suspend fun removeItems(ids: List<String>) = groupWrites.write { groupId ->
        val collection = items(groupId)
        // A batch holds at most 500 writes.
        ids.chunked(MAX_BATCH_WRITES).forEach { chunk ->
            firestore.batch().apply { chunk.forEach { id -> delete(collection.document(id)) } }.commit().await()
        }
    }

    /** The data of the document, with the time of an item that was just added (and not yet confirmed by the server) estimated. */
    private fun DocumentSnapshot.estimatedData(): Map<String, Any?>? = getData(ServerTimestampBehavior.ESTIMATE)

    private fun items(groupId: String): CollectionReference =
        firestore.collection(GroupPaths.GROUPS).document(groupId).collection(SHOPPING_COLLECTION)

    private companion object {
        const val MAX_BATCH_WRITES = 500
    }
}
