package com.mindInk.app.ui.articles.model

data class ArticleCardUi(
    val id: String,
    val category: String,
    val title: String,
    val summary: String,
    val authorName: String,
    val authorPhotoUrl: String?,
    val starCount: Int,
    val viewCount: Int,
    val starredBy: List<String>,
    val createdAt: Long
)
