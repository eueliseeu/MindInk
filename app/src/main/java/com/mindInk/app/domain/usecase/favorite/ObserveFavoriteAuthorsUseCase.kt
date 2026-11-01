package com.mindInk.app.domain.usecase.favorite

import com.mindInk.app.domain.repository.FavoriteAuthorRepository
import javax.inject.Inject

class ObserveFavoriteAuthorsUseCase @Inject constructor(
    private val repository: FavoriteAuthorRepository
) {
    operator fun invoke(uid: String, limit: Long = 20) =
        repository.observeFavoriteAuthors(uid, limit)
}