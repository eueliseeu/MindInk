package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.ArticleRepository
import javax.inject.Inject

class ObserveArticlesByAuthorsUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    operator fun invoke(authorUids: List<String>, limit: Long = 20) =
        repository.observeArticlesByAuthors(authorUids, limit)
}