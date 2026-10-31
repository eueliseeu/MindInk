package com.mindInk.app.domain.model

data class ReadingProgress(
    val articleId: String,
    val title: String,
    val category: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String? = null,
    val progress: Float = 0f,
    val updatedAt: Long = 0L
) {
    val isCompleted: Boolean get() = progress >= 1f
}