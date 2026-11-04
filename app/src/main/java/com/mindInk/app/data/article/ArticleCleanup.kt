package com.mindInk.app.data.article

import com.google.firebase.firestore.FirebaseFirestore
import com.mindInk.app.data.common.deleteInBatches
import kotlinx.coroutines.tasks.await

internal suspend fun FirebaseFirestore.deleteArticleCascade(articleId: String) {
    val articleRef = collection(ARTICLES_COLLECTION).document(articleId)

    deleteInBatches(articleRef.collection(ARTICLE_VIEWS_SUBCOLLECTION))

    val batch = batch()
    batch.delete(articleContentRef(articleId))
    batch.delete(articleRef)
    batch.commit().await()
}