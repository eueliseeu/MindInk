package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.ArticleRepository
import javax.inject.Inject

class ObserveArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    operator fun invoke(limit: Long = 50) = repository.observeArticles(limit)
}
