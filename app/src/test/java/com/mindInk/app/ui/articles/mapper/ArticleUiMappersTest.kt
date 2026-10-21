package com.mindInk.app.ui.articles.mapper

import com.mindInk.app.domain.model.Article
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleUiMappersTest {

    @Test
    fun toCardUiUsesCategoryAndContentPreviewWhenOptionalFieldsAreBlank() {
        val article = article(
            category = "",
            subtitle = "",
            content = "Resumo do conteúdo"
        )

        val card = article.toCardUi()

        assertEquals("Tech", card.category)
        assertEquals("Resumo do conteúdo", card.summary)
        assertEquals("Ana Silva", card.authorName)
    }

    @Test
    fun toCardUiPrefersSubtitleAndPreservesStarData() {
        val article = article(
            category = "Kotlin",
            subtitle = "Resumo curto",
            content = "Conteúdo completo",
            starredBy = listOf("user-1"),
            starCount = 1
        )

        val card = article.toCardUi()

        assertEquals("Kotlin", card.category)
        assertEquals("Resumo curto", card.summary)
        assertEquals(listOf("user-1"), card.starredBy)
        assertEquals(1, card.starCount)
    }

    private fun article(
        category: String,
        subtitle: String,
        content: String,
        starredBy: List<String> = emptyList(),
        starCount: Int = 0
    ) = Article(
        id = "article-1",
        authorUid = "user-1",
        authorName = "Ana Silva Rocha",
        authorUsername = "ana",
        category = category,
        title = "Título",
        subtitle = subtitle,
        content = content,
        starredBy = starredBy,
        starCount = starCount,
        createdAt = 1L
    )
}
