package com.mindInk.app.domain.model

data class AuthUser(
    val uid: String,
    val email: String?,
    val provider: AuthProvider
)
