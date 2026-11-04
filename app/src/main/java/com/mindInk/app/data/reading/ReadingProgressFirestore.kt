package com.mindInk.app.data.reading

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mindInk.app.data.profile.users

private const val READING_PROGRESS_COLLECTION = "readingProgress"

internal const val READING_FIELD_ARTICLE_ID = "articleId"
internal const val READING_FIELD_PROGRESS = "progress"
internal const val READING_FIELD_COMPLETED = "completed"
internal const val READING_FIELD_UPDATED_AT = "updatedAt"

internal fun FirebaseFirestore.readingProgress(uid: String): CollectionReference =
    users().document(uid).collection(READING_PROGRESS_COLLECTION)