package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.ArticleRepository
import javax.inject.Inject

class ObserveArticleContentUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    operator fun invoke(articleId: String) = repository.observeArticleContent(articleId)
}