package com.mindInk.app.data.profile

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

private const val USERS_COLLECTION = "users"

internal const val USER_FIELD_NAME = "name"
internal const val USER_FIELD_PHOTO_URL = "photoUrl"
internal const val USER_FIELD_GITHUB = "github"
internal const val USER_FIELD_LINKEDIN = "linkedin"
internal const val USER_FIELD_CREATED_AT = "createdAt"
internal const val USER_FIELD_UPDATED_AT = "updatedAt"

internal fun FirebaseFirestore.users(): CollectionReference = collection(USERS_COLLECTION)