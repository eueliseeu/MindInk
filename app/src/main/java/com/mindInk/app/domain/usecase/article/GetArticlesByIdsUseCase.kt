package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.ArticleRepository
import javax.inject.Inject

class GetArticlesByIdsUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(articleIds: Collection<String>) =
        repository.getArticlesByIds(articleIds)
}