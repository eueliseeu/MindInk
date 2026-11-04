package com.mindInk.app.data.favorite

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.mindInk.app.data.profile.users

private const val FAVORITE_AUTHORS_COLLECTION = "favoriteAuthors"

internal const val FIELD_FAVORITED_AT = "favoritedAt"
private const val FIELD_STARRED_COUNT = "starredCount"

internal fun FirebaseFirestore.favoriteAuthors(uid: String): CollectionReference =
    users().document(uid).collection(FAVORITE_AUTHORS_COLLECTION)

internal fun Transaction.applyStarToFavorite(
    favoriteRef: DocumentReference,
    favorite: DocumentSnapshot,
    starred: Boolean
) {
    val count = favorite.getLong(FIELD_STARRED_COUNT)?.toInt() ?: 0

    when {
        starred && favorite.exists() ->
            update(favoriteRef, FIELD_STARRED_COUNT, count + 1)

        starred -> set(
            favoriteRef,
            mapOf(
                FIELD_STARRED_COUNT to 1,
                FIELD_FAVORITED_AT to FieldValue.serverTimestamp()
            )
        )

        favorite.exists() && count > 1 ->
            update(favoriteRef, FIELD_STARRED_COUNT, count - 1)

        favorite.exists() -> delete(favoriteRef)
    }
}