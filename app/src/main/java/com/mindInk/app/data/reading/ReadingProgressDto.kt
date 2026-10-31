package com.mindInk.app.data.reading

data class ReadingProgressDto(
    val articleId: String = "",
    val title: String = "",
    val category: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String? = null,
    val progress: Float = 0f,
    val updatedAt: Long = 0L
)