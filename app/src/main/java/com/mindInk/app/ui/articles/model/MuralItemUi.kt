package com.mindInk.app.ui.articles.model

sealed interface MuralItemUi {
    val id: String
    val actorUid: String
    val actorName: String
    val actorPhotoUrl: String?
    val articleId: String
    val articleTitle: String
    val sortKey: Long

    data class StarReceived(
        override val id: String,
        override val actorUid: String,
        override val actorName: String,
        override val actorPhotoUrl: String?,
        override val articleId: String,
        override val articleTitle: String,
        override val sortKey: Long
    ) : MuralItemUi

    data class NewArticleFromFavorite(
        override val id: String,
        override val actorUid: String,
        override val actorName: String,
        override val actorPhotoUrl: String?,
        override val articleId: String,
        override val articleTitle: String,
        override val sortKey: Long
    ) : MuralItemUi
}