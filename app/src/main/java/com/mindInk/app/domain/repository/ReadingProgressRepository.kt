package com.mindInk.app.domain.repository

import com.mindInk.app.domain.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

sealed interface UpsertReadingResult {
    data object Success : UpsertReadingResult
    data class Error(val message: String) : UpsertReadingResult
}

sealed interface ObserveReadingResult {
    data class Success(val items: List<ReadingProgress>) : ObserveReadingResult
    data class Error(val message: String) : ObserveReadingResult
}

interface ReadingProgressRepository {
    fun observeContinueReading(uid: String, limit: Long = 5): Flow<ObserveReadingResult>

    suspend fun upsertProgress(
        uid: String,
        articleId: String,
        title: String,
        category: String,
        authorName: String,
        authorPhotoUrl: String?,
        progress: Float
    ): UpsertReadingResult

    suspend fun markCompleted(uid: String, articleId: String): UpsertReadingResult
}