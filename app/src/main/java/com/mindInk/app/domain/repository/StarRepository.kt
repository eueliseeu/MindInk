package com.mindInk.app.domain.repository

import com.mindInk.app.domain.model.ReceivedStar
import kotlinx.coroutines.flow.Flow

sealed interface ToggleStarResult {
    data object Success : ToggleStarResult
    data class Error(val message: String) : ToggleStarResult
}

sealed interface ObserveReceivedStarsResult {
    data class Success(val stars: List<ReceivedStar>) : ObserveReceivedStarsResult
    data class Error(val message: String) : ObserveReceivedStarsResult
}

interface StarRepository {
    fun observeStarredArticleIds(uid: String): Flow<Set<String>>

    fun observeReceivedStars(
        authorUid: String,
        limit: Long = 10
    ): Flow<ObserveReceivedStarsResult>

    suspend fun toggleStar(articleId: String, uid: String): ToggleStarResult
}