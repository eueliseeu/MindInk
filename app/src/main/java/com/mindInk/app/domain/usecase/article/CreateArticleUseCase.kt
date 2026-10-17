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
        authorPhotoUrl: String?,
        category: String,
        title: String,
        subtitle: String,
        content: String,
        linkUrl: String = ""
    ): CreateArticleResult {
        val t = title.trim()
        val c = content.trim()
        val cat = category.trim()
        if (cat.isEmpty()) {
            return CreateArticleResult.Error("Selecione a categoria do artigo.")
        }
        if (t.isEmpty() || c.isEmpty()) {
            return CreateArticleResult.Error("Título e conteúdo são obrigatórios.")
        }
        return repository.createArticle(
            authorUid = authorUid,
            authorName = authorName,
            authorUsername = authorUsername,
            authorPhotoUrl = authorPhotoUrl,
            category = cat,
            title = t,
            subtitle = subtitle.trim(),
            content = c,
            linkUrl = linkUrl.trim()
        )
    }
}
