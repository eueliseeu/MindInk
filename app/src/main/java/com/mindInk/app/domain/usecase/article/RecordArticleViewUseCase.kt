package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.ArticleRepository
import javax.inject.Inject

class RecordArticleViewUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(articleId: String, viewerUid: String) =
        repository.recordView(articleId, viewerUid)
}
