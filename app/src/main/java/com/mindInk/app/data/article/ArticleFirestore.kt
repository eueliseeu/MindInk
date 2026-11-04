package com.mindInk.app.data.article

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

internal const val ARTICLES_COLLECTION = "articles"
internal const val ARTICLE_VIEWS_SUBCOLLECTION = "views"

private const val ARTICLE_CONTENT_SUBCOLLECTION = "content"
private const val ARTICLE_CONTENT_DOC_ID = "body"

internal const val ARTICLE_CONTENT_FIELD = "text"

internal fun FirebaseFirestore.articleContentRef(articleId: String): DocumentReference =
    collection(ARTICLES_COLLECTION)
        .document(articleId)
        .collection(ARTICLE_CONTENT_SUBCOLLECTION)
        .document(ARTICLE_CONTENT_DOC_ID)