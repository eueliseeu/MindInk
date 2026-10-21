package com.mindInk.app.data.article

data class ArticleDto(
    val authorUid: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorPhotoUrl: String? = null,
    val category: String = "",
    val title: String = "",
    val subtitle: String = "",
    val content: String = "",
    val linkUrl: String = "",
    val starredBy: List<String> = emptyList(),
    val starCount: Int = 0,
    val viewCount: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
