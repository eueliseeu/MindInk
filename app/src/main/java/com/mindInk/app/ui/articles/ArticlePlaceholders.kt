package com.mindInk.app.ui.articles

import com.mindInk.app.ui.articles.model.ArticleCardUi

private const val HOUR_MS = 3_600_000L
private const val DAY_MS = 24 * HOUR_MS

fun articlePlaceholders(nowMillis: Long = System.currentTimeMillis()): List<ArticleCardUi> = listOf(
    ArticleCardUi(
        id = "placeholder_1",
        category = "IA.",
        title = "LLM's sendo utilizados para Estudos/ Faculdade",
        summary = "Estudos mostra que as LLM's estão prejudicando",
        authorName = "Eliseu S.",
        authorPhotoUrl = null,
        starCount = 3_100,
        createdAt = nowMillis - 2 * DAY_MS
    ),
    ArticleCardUi(
        id = "placeholder_2",
        category = "IA.",
        title = "Nova IA no Mercado Revoluciona GPT 5.1",
        summary = "ChatGPT fica para trás na Anthropic Claude",
        authorName = "Marcelo G.",
        authorPhotoUrl = null,
        starCount = 1_850,
        createdAt = nowMillis - 5 * HOUR_MS
    ),
    ArticleCardUi(
        id = "placeholder_3",
        category = "Kotlin",
        title = "Kotlin Multiplatform em produção: vale a pena?",
        summary = "Relatos de times que migraram e o que mudou no dia a dia",
        authorName = "Ana P.",
        authorPhotoUrl = null,
        starCount = 920,
        createdAt = nowMillis - 5 * DAY_MS
    ),
    ArticleCardUi(
        id = "placeholder_4",
        category = "Firebase",
        title = "Security Rules do Firestore sem dor de cabeça",
        summary = "Padrões simples para proteger os dados do seu app",
        authorName = "Lucas M.",
        authorPhotoUrl = null,
        starCount = 540,
        createdAt = nowMillis - 40 * 60_000L
    )
)
