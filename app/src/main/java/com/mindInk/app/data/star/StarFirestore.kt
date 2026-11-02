package com.mindInk.app.data.star

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mindInk.app.data.profile.users

internal const val STARS_COLLECTION = "stars"

internal const val STAR_FIELD_ARTICLE_ID = "articleId"
internal const val STAR_FIELD_AUTHOR_UID = "authorUid"
internal const val STAR_FIELD_CREATED_AT = "createdAt"

internal fun FirebaseFirestore.stars(uid: String): CollectionReference =
    users().document(uid).collection(STARS_COLLECTION)