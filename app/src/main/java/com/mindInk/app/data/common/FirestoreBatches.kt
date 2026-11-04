package com.mindInk.app.data.common

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

private const val DELETE_BATCH_SIZE = 400L

internal suspend fun FirebaseFirestore.deleteInBatches(query: Query) {
    while (true) {
        val snapshot = query.limit(DELETE_BATCH_SIZE).get(Source.SERVER).await()
        if (snapshot.isEmpty) return

        val batch = batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }
}