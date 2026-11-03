package com.tavola.data.firestore

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

// The listeners of Firestore as flows. The `snapshots()` of the library cancels its flow with a CancellationException when the
// server refuses or fails a listener (rules that do not allow the read, say). Cancellation is not an error to the operators of
// flows: a `combine` of two listeners just waits for the one that died, so the screen shows its loading state forever and `catch`
// never runs. These close the flow with the real exception, so it reaches the `catch` of whoever collects it. A snapshot is the whole
// state, so the flows are conflated: a slow collector gets the latest one, never a stale one in its place.

/** The document, now and every time it changes. A refusal of the server ends the flow with its `FirebaseFirestoreException`. */
internal fun DocumentReference.snapshotFlow(metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE): Flow<DocumentSnapshot> =
    callbackFlow {
        val registration = addSnapshotListener(metadataChanges) { snapshot, error ->
            if (error != null) {
                close(error)
            } else if (snapshot != null) {
                trySend(snapshot)
            }
        }
        awaitClose { registration.remove() }
    }.buffer(Channel.CONFLATED)

/** The result of the query, now and every time it changes. A refusal of the server ends the flow with its `FirebaseFirestoreException`. */
internal fun Query.snapshotFlow(metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener(metadataChanges) { snapshot, error ->
        if (error != null) {
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}.buffer(Channel.CONFLATED)
