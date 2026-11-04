package com.mindInk.app.domain.model

data class ReadingProgress(
    val articleId: String,
    val progress: Float = 0f,
    val updatedAt: Long = 0L
) {
    val isCompleted: Boolean get() = progress >= 1f
}