package com.mindInk.app.domain.model

data class ReceivedStar(
    val articleId: String,
    val starerUid: String,
    val createdAt: Long
)