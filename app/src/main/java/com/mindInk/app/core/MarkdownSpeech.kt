package com.mindInk.app.core

fun markdownToSpeechText(
    title: String,
    subtitle: String,
    content: String
): String {
    val body = content
        .replace(Regex("!\\[[^\\]]*]\\([^)]*\\)"), " ")
        .replace(Regex("\\[([^\\]]+)]\\([^)]*\\)"), "$1")
        .replace(Regex("```[\\s\\S]*?```"), " ")
        .replace(Regex("`[^`]+`"), " ")
        .replace(Regex("#{1,6}\\s*"), "")
        .replace(Regex("\\*{1,3}|_{1,3}"), "")
        .replace(Regex("^>\\s*", RegexOption.MULTILINE), "")
        .replace(Regex("^[-*+]\\s+", RegexOption.MULTILINE), "")
        .replace(Regex("\\d+\\.\\s+"), "")
        .replace(Regex("[|\\-]{2,}"), " ")
        .replace(Regex("\\n+"), ". ")
        .replace(Regex("\\s{2,}"), " ")
        .replace(Regex("\\.{2,}"), ".")
        .trim()

    return buildString {
        if (title.isNotBlank()) {
            append(title.trim())
            append(". ")
        }
        if (subtitle.isNotBlank()) {
            append(subtitle.trim())
            append(". ")
        }
        append(body)
    }.trim()
}
