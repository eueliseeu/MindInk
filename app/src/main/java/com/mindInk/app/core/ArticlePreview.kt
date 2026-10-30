package com.mindInk.app.core

fun previewHalfContent(content: String): String {
    val trimmed = content.trim()
    if (trimmed.isEmpty()) return ""
    val half = (trimmed.length * 0.5f).toInt().coerceAtLeast(1)
    var cut = half
    while (cut < trimmed.length && !trimmed[cut].isWhitespace()) {
        cut++
    }
    val preview = trimmed.take(cut).trimEnd()
    return if (cut < trimmed.length) "$preview…" else preview
}