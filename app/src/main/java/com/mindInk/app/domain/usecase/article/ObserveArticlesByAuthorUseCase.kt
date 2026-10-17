package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.ArticleRepository
import javax.inject.Inject

class ObserveArticlesByAuthorUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    operator fun invoke(authorUid: String, limit: Long = 50) =
        repository.observeArticlesByAuthor(authorUid, limit)
}
