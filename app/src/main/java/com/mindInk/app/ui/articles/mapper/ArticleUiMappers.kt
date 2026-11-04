package com.mindInk.app.ui.articles.mapper

import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.model.Article
import com.mindInk.app.ui.articles.model.ArticleCardUi

fun Article.toCardUi(): ArticleCardUi = ArticleCardUi(
    id = id,
    category = category.ifBlank { "Tech" },
    title = title,
    summary = subtitle.ifBlank { excerpt },
    authorName = formatAuthorName(authorName),
    authorPhotoUrl = authorPhotoUrl,
    starCount = starCount,
    viewCount = viewCount,
    createdAt = createdAt
)