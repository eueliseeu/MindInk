package com.mindInk.app.domain.model

data class Article(
    val id: String,
    val authorUid: String,
    val authorName: String,
    val authorPhotoUrl: String? = null,
    val category: String = "",
    val title: String,
    val subtitle: String = "",
    val excerpt: String = "",
    val starCount: Int = 0,
    val viewCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long = 0L
)