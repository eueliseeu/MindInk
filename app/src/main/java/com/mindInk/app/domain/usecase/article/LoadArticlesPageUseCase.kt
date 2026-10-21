package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.ArticleRepository
import com.mindInk.app.domain.repository.LoadArticlesPageResult
import javax.inject.Inject

class LoadArticlesPageUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(
        pageSize: Long = 10,
        startAfterDocumentId: String? = null
    ): LoadArticlesPageResult =
        repository.loadArticlesPage(pageSize, startAfterDocumentId)
}
