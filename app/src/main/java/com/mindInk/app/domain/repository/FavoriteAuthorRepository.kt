package com.mindInk.app.domain.repository

import com.mindInk.app.domain.model.FavoriteAuthor
import kotlinx.coroutines.flow.Flow

sealed interface ObserveFavoriteAuthorsResult {
    data class Success(val authors: List<FavoriteAuthor>) : ObserveFavoriteAuthorsResult
    data class Error(val message: String) : ObserveFavoriteAuthorsResult
}

interface FavoriteAuthorRepository {
    fun observeFavoriteAuthors(
        uid: String,
        limit: Long = 20
    ): Flow<ObserveFavoriteAuthorsResult>
}