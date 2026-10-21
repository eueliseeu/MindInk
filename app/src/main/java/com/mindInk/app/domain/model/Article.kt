package com.mindInk.app.domain.model

data class Article(
    val id: String,
    val authorUid: String,
    val authorName: String,
    val authorUsername: String,
    val authorPhotoUrl: String? = null,
    val category: String = "",
    val title: String,
    val subtitle: String = "",
    val content: String,
    val linkUrl: String = "",
    val starredBy: List<String> = emptyList(),
    val starCount: Int = 0,
    val viewCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long = 0L
) {
    fun preview(maxChars: Int = 160): String {
        val text = content.trim()
        if (text.length <= maxChars) return text
        return text.take(maxChars).trimEnd() + "…"
    }

    fun isStarredBy(uid: String?): Boolean =
        uid != null && starredBy.contains(uid)
}
