package com.mindInk.app.data.article

import com.google.firebase.Timestamp

data class ArticleDto(
    val authorUid: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String? = null,
    val category: String = "",
    val title: String = "",
    val subtitle: String = "",
    val excerpt: String = "",
    val starCount: Int = 0,
    val viewCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)