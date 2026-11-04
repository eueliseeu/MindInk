package com.mindInk.app.core

const val ARTICLE_EXCERPT_LENGTH = 160

fun buildExcerpt(content: String, maxChars: Int = ARTICLE_EXCERPT_LENGTH): String {
    val text = content.trim()
    if (text.length <= maxChars) return text
    return text.take(maxChars).trimEnd() + "…"
}