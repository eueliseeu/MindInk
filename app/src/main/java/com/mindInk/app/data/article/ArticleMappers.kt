package com.mindInk.app.data.article

import com.google.firebase.firestore.DocumentSnapshot
import com.mindInk.app.domain.model.Article

internal fun DocumentSnapshot.toArticle(): Article? {
    val data = toObject(ArticleDto::class.java) ?: return null
    if (data.title.isBlank() || data.content.isBlank()) return null
    return Article(
        id = id,
        authorUid = data.authorUid,
        authorName = data.authorName,
        authorPhotoUrl = data.authorPhotoUrl,
        category = data.category,
        title = data.title,
        subtitle = data.subtitle,
        content = data.content,
        starredBy = data.starredBy,
        starCount = data.starCount,
        viewCount = data.viewCount,
        createdAt = data.createdAt,
        updatedAt = data.updatedAt
    )
}