package com.mindInk.app.data.article

import com.google.firebase.firestore.DocumentSnapshot
import com.mindInk.app.domain.model.Article
import com.mindInk.app.ui.articles.model.ArticleCardUi

internal fun DocumentSnapshot.toArticle(): Article? {
    val data = toObject(ArticleDto::class.java) ?: return null
    if (data.title.isBlank() || data.content.isBlank()) return null
    return Article(
        id = id,
        authorUid = data.authorUid,
        authorName = data.authorName,
        authorUsername = data.authorUsername,
        authorPhotoUrl = data.authorPhotoUrl,
        category = data.category,
        title = data.title,
        subtitle = data.subtitle,
        content = data.content,
        linkUrl = data.linkUrl,
        starredBy = data.starredBy,
        starCount = data.starCount,
        createdAt = data.createdAt,
        updatedAt = data.updatedAt
    )
}

internal fun Article.toDto(): ArticleDto = ArticleDto(
    authorUid = authorUid,
    authorName = authorName,
    authorUsername = authorUsername,
    authorPhotoUrl = authorPhotoUrl,
    category = category,
    title = title,
    subtitle = subtitle,
    content = content,
    linkUrl = linkUrl,
    starredBy = starredBy,
    starCount = starCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Article.toCardUi(): ArticleCardUi = ArticleCardUi(
    id = id,
    category = category.ifBlank { "Tech" },
    title = title,
    summary = subtitle.ifBlank { preview(120) },
    authorName = authorName,
    authorPhotoUrl = authorPhotoUrl,
    starCount = starCount,
    createdAt = createdAt
)
