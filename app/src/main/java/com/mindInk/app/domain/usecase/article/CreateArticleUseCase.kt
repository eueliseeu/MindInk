// domain/usecase/article/CreateArticleUseCase.kt
package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.ArticleRepository
import com.mindInk.app.domain.repository.CreateArticleResult
import javax.inject.Inject

class CreateArticleUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(
        authorUid: String,
        authorName: String,
        authorUsername: String,
        title: String,
        content: String,
        linkUrl: String
    ): CreateArticleResult {
        val t = title.trim()
        val c = content.trim()
        if (t.isEmpty() || c.isEmpty()) {
            return CreateArticleResult.Error("Título e conteúdo são obrigatórios.")
        }
        return repository.createArticle(
            authorUid = authorUid,
            authorName = authorName,
            authorUsername = authorUsername,
            title = t,
            content = c,
            linkUrl = linkUrl.trim()
        )
    }
}
