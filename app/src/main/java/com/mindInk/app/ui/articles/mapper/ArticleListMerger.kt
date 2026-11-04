package com.mindInk.app.ui.articles.mapper

import com.mindInk.app.ui.articles.model.ArticleCardUi

fun mergeRealtimeArticles(
    current: List<ArticleCardUi>,
    observed: List<ArticleCardUi>,
    windowFull: Boolean,
    category: String? = null
): List<ArticleCardUi> {
    val observedMap = observed.associateBy { it.id }
    val oldestObserved = observed.minOfOrNull { it.createdAt }
    val currentIds = current.map { it.id }.toHashSet()

    val kept = current.filter { card ->
        val live = observedMap[card.id]
        when {
            live != null -> category == null || live.category == category
            !windowFull -> false
            oldestObserved == null -> true
            else -> card.createdAt < oldestObserved
        }
    }
    val updated = kept.map { card -> observedMap[card.id] ?: card }

    val candidates = if (category == null) observed else observed.filter { it.category == category }
    val brandNew = candidates.filter { it.id !in currentIds }

    return (brandNew + updated).sortedByDescending { it.createdAt }
}