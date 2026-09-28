package com.mindInk.app.domain.usecase.article

import com.mindInk.app.domain.repository.ArticleRepository
import com.mindInk.app.domain.repository.UpdateArticleResult
import javax.inject.Inject

class UpdateArticleUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(
        articleId: String,
        authorUid: String,
        title: String,
        content: String,
        linkUrl: String
    ): UpdateArticleResult {
        val t = title.trim()
        val c = content.trim()
        if (t.isEmpty() || c.isEmpty()) {
            return UpdateArticleResult.Error("Título e conteúdo são obrigatórios.")
        }
        return repository.updateArticle(
            articleId = articleId,
            authorUid = authorUid,
            title = t,
            content = c,
            linkUrl = linkUrl.trim()
        )
    }
}
