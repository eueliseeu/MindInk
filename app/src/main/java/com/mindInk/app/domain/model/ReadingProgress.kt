package com.mindInk.app.domain.model

data class ReadingProgress(
    val articleId: String,
    val title: String,
    val category: String = "",
    val summary: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String? = null,
    val starCount: Int = 0,
    val viewCount: Int = 0,
    val progress: Float = 0f,
    val updatedAt: Long = 0L
) {
    val isCompleted: Boolean get() = progress >= 1f
}