package com.mindInk.app.core

fun formatAuthorName(raw: String?): String {
    if (raw.isNullOrBlank()) return "Autor"
    val parts = raw
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "Autor"
        parts.size == 1 -> parts[0].take(24)
        else -> "${parts[0].take(24)} ${parts[1].take(24)}"
    }
}
