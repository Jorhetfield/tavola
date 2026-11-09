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
import com.tavola.domain.model.ShoppingCatalog
import com.tavola.domain.model.ShoppingHistory
import com.tavola.domain.model.ShoppingHistoryEntry
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
 * Adding an item also counts a use in the group's history (`history/{key}`) in the same batch, and buying one deletes it and notes
 * the purchase there.
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
                    // Items added in one batch share their time, so they are told apart by name and then by id.
                    .sortedWith(compareBy({ item -> item.createdAt }, { item -> item.name.lowercase() }, { item -> item.id }))
            }
        }
    }

    override suspend fun saveItem(item: ShoppingItem): String {
        val clean = item.normalized()
        val groupId = groupWrites.requireGroupId("item")
        val collection = items(groupId)
        val isNew = clean.id.isBlank()
        val reference = if (isNew) collection.document() else collection.document(clean.id)
        // Only a new item is stamped (with the server's time), signed and counted in the history; a restored one keeps its time, and
        // the merge leaves whoever added it as they were.
        if (isNew) {
            val document = clean.toDocument(authRepository.currentUser.first()?.id, createdAtValue = FieldValue.serverTimestamp())
            groupWrites.write {
                firestore.batch().apply {
                    set(reference, document, SetOptions.merge())
                    set(history(groupId).document(ShoppingHistory.keyFor(clean.name)), clean.toUseDocument(), SetOptions.merge())
                }.commit().await()
            }
        } else {
            groupWrites.write { reference.set(clean.toDocument(clean.addedBy), SetOptions.merge()).await() }
        }
        return reference.id
    }

    override suspend fun addItems(items: List<ShoppingItem>) {
        if (items.isEmpty()) return
        val groupId = groupWrites.requireGroupId("items")
        val collection = items(groupId)
        val historyCollection = history(groupId)
        val addedBy = authRepository.currentUser.first()?.id
        val clean = items.map { item -> item.normalized() }
        groupWrites.write {
            // A batch holds at most 500 writes, and each item takes two (itself and its history). Each batch is all or nothing.
            clean.chunked(MAX_BATCH_WRITES / WRITES_PER_ITEM).forEach { chunk ->
                firestore.batch().apply {
                    chunk.forEach { item ->
                        set(collection.document(), item.toDocument(addedBy, createdAtValue = FieldValue.serverTimestamp()))
                        set(historyCollection.document(ShoppingHistory.keyFor(item.name)), item.toUseDocument(), SetOptions.merge())
                    }
                }.commit().await()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeHistory(): Flow<List<ShoppingHistoryEntry>> = groupRepository.groupId.flatMapLatest { groupId ->
        if (groupId == null) {
            flowOf(emptyList())
        } else {
            history(groupId).snapshotFlow().map { snapshot ->
                snapshot.documents
                    .mapNotNull { document -> document.estimatedData()?.toHistoryEntry(document.id) }
                    .sortedWith(
                        compareByDescending<ShoppingHistoryEntry> { entry -> entry.lastUsedAt }
                            .thenByDescending { entry -> entry.useCount }
                            .thenBy { entry -> entry.name.lowercase() },
                    )
            }
        }
    }

    override suspend fun buyItem(item: ShoppingItem) {
        require(item.id.isNotBlank()) { "Only an item of the list can be bought" }
        val clean = item.normalized()
        groupWrites.write { groupId ->
            firestore.batch().apply {
                delete(items(groupId).document(clean.id))
                set(history(groupId).document(ShoppingHistory.keyFor(clean.name)), clean.toBoughtDocument(), SetOptions.merge())
            }.commit().await()
        }
    }

    override suspend fun forgetHistoryEntry(entry: ShoppingHistoryEntry) = groupWrites.write { groupId ->
        val reference = history(groupId).document(entry.id)
        if (ShoppingCatalog.find(entry.name) != null) {
            reference.set(entry.toHiddenDocument(), SetOptions.merge()).await()
        } else {
            reference.delete().await()
        }
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

    private fun history(groupId: String): CollectionReference =
        firestore.collection(GroupPaths.GROUPS).document(groupId).collection(HISTORY_COLLECTION)

    private companion object {
        const val MAX_BATCH_WRITES = 500
        const val WRITES_PER_ITEM = 2
    }
}
