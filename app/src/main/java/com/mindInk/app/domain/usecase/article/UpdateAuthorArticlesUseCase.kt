package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.ArticleRepository
import javax.inject.Inject

class UpdateAuthorArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(authorUid: String, authorName: String) =
        repository.updateAuthorName(authorUid, authorName)
}
