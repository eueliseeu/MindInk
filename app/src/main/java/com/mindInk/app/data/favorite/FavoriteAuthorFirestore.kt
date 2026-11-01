package com.mindInk.app.data.favorite

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction

private const val ROOT = "user_favorite_authors"
private const val ITEMS = "items"

internal const val FIELD_FAVORITED_AT = "favoritedAt"
private const val FIELD_STARRED_COUNT = "starredCount"
private const val FIELD_AUTHOR_NAME = "authorName"
private const val FIELD_AUTHOR_PHOTO_URL = "authorPhotoUrl"

internal fun FirebaseFirestore.favoriteAuthors(uid: String): CollectionReference =
    collection(ROOT).document(uid).collection(ITEMS)


internal fun Transaction.applyStarToFavorite(
    favoriteRef: DocumentReference,
    favorite: DocumentSnapshot,
    article: DocumentSnapshot,
    starred: Boolean,
    now: Long
) {
    val count = favorite.getLong(FIELD_STARRED_COUNT)?.toInt() ?: 0
    val authorName = article.getString(FIELD_AUTHOR_NAME).orEmpty()
    val authorPhotoUrl = article.getString(FIELD_AUTHOR_PHOTO_URL)

    when {
        starred && favorite.exists() -> update(
            favoriteRef,
            mapOf(
                FIELD_STARRED_COUNT to count + 1,
                FIELD_AUTHOR_NAME to authorName,
                FIELD_AUTHOR_PHOTO_URL to authorPhotoUrl
            )
        )

        starred -> set(
            favoriteRef,
            FavoriteAuthorDto(
                authorUid = favoriteRef.id,
                authorName = authorName,
                authorPhotoUrl = authorPhotoUrl,
                starredCount = 1,
                favoritedAt = now
            )
        )

        favorite.exists() && count > 1 ->
            update(favoriteRef, FIELD_STARRED_COUNT, count - 1)

        favorite.exists() -> delete(favoriteRef)
    }
}