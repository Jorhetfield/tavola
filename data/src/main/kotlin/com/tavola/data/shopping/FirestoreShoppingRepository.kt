package com.tavola.data.shopping

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.tavola.data.firestore.GroupWrites
import com.tavola.data.firestore.snapshotFlow
import com.tavola.data.group.GroupPaths
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
 * Adding an item also counts a use in the group's history (`history/{key}`), and buying one deletes it and notes the purchase there.
 * Those history writes are queued but not waited for, and are never part of the write of the items.
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
        val document = if (isNew) {
            clean.toDocument(authRepository.currentUser.first()?.id, createdAtValue = FieldValue.serverTimestamp())
        } else {
            clean.toDocument(clean.addedBy)
        }
        groupWrites.write { reference.set(document, SetOptions.merge()).await() }
        if (isNew) recordUses(groupId, listOf(clean))
        return reference.id
    }

    override suspend fun addItems(items: List<ShoppingItem>) {
        if (items.isEmpty()) return
        val groupId = groupWrites.requireGroupId("items")
        val collection = items(groupId)
        val addedBy = authRepository.currentUser.first()?.id
        val clean = items.map { item -> item.normalized() }
        groupWrites.write {
            // A batch holds at most 500 writes. Each batch is all or nothing.
            clean.chunked(MAX_BATCH_WRITES).forEach { chunk ->
                firestore.batch().apply {
                    chunk.forEach { item ->
                        set(collection.document(), item.toDocument(addedBy, createdAtValue = FieldValue.serverTimestamp()))
                    }
                }.commit().await()
            }
        }
        recordUses(groupId, clean)
    }

    override fun observeHistory(): Flow<List<ShoppingHistoryEntry>> = groupRepository.groupId.flatMapLatest { groupId ->
        if (groupId == null) {
            flowOf(emptyList())
        } else {
            // Only the entries used most recently are read, so the listener stays small however long the group has been using the app.
            history(
                groupId,
            ).orderBy(HistoryFields.LAST_USED_AT, Query.Direction.DESCENDING).limit(HISTORY_LIMIT).snapshotFlow().map { snapshot ->
                snapshot.documents
                    .mapNotNull { document -> document.estimatedData()?.toHistoryEntry(document.id) }
                    .sortedWith(ShoppingHistory.mostRecentFirst)
            }
        }
    }

    override suspend fun buyItem(item: ShoppingItem) {
        require(item.id.isNotBlank()) { "Only an item of the list can be bought" }
        val clean = item.normalized()
        groupWrites.write { groupId ->
            // The history is queued first and not waited for: if the rules refuse it, the purchase still takes the item off the list.
            history(groupId).document(ShoppingHistory.keyFor(clean.name)).set(clean.toBoughtDocument(), SetOptions.merge())
            items(groupId).document(clean.id).delete().await()
        }
    }

    override suspend fun forgetHistoryEntry(entry: ShoppingHistoryEntry) = groupWrites.write { groupId ->
        val reference = history(groupId).document(entry.id)
        if (entry.isFromCatalog) {
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

    /**
     * Counts a use in the history for each of [items]. It is not waited for, and it is not in the batch of the items: the history is
     * a convenience, so a refusal of the server (the rules for it not published yet, say) must neither undo nor delay adding them.
     */
    private fun recordUses(groupId: String, items: List<ShoppingItem>) {
        val collection = history(groupId)
        items.chunked(MAX_BATCH_WRITES).forEach { chunk ->
            firestore.batch().apply {
                chunk.forEach { item ->
                    set(collection.document(ShoppingHistory.keyFor(item.name)), item.toUseDocument(), SetOptions.merge())
                }
            }.commit()
        }
    }

    private fun history(groupId: String): CollectionReference =
        firestore.collection(GroupPaths.GROUPS).document(groupId).collection(HISTORY_COLLECTION)

    private companion object {
        const val MAX_BATCH_WRITES = 500

        /** How many history entries are read, the most recently used first. */
        const val HISTORY_LIMIT = 300L
    }
}
