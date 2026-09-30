package com.mindInk.app.domain.repository

import android.app.Activity
import com.mindInk.app.domain.model.AuthProvider
import com.mindInk.app.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

enum class AuthErrorReason {
    NETWORK,
    NO_GOOGLE_ACCOUNT,
    ACCOUNT_COLLISION,
    INVALID_CREDENTIAL,
    UNKNOWN
}

sealed interface SignInResult {
    data class Success(val user: AuthUser) : SignInResult
    data object Cancelled : SignInResult
    data class Error(val reason: AuthErrorReason) : SignInResult

    data class LinkRequired(
        val pendingProvider: AuthProvider,
        val existingProvider: AuthProvider
    ) : SignInResult
}

interface AuthRepository {
    fun observeAuthState(): Flow<AuthUser?>

    suspend fun signInWithGoogle(activity: Activity): SignInResult

    suspend fun signInWithGitHub(activity: Activity): SignInResult

    suspend fun linkPendingAccount(activity: Activity): SignInResult

    fun discardPendingLink()

    suspend fun signOut()
}
