package com.mindInk.app.data.article

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.mindInk.app.domain.model.Article

internal fun DocumentSnapshot.toArticle(): Article? {
    val data = toObject(ArticleDto::class.java, ServerTimestampBehavior.ESTIMATE) ?: return null
    if (data.title.isBlank()) return null
    return Article(
        id = id,
        authorUid = data.authorUid,
        authorName = data.authorName,
        authorPhotoUrl = data.authorPhotoUrl,
        category = data.category,
        title = data.title,
        subtitle = data.subtitle,
        excerpt = data.excerpt,
        starCount = data.starCount,
        viewCount = data.viewCount,
        createdAt = data.createdAt.toMillis(),
        updatedAt = data.updatedAt.toMillis()
    )
}

private fun Timestamp?.toMillis(): Long = this?.toDate()?.time ?: 0L