package com.mindInk.app.ui.auth

import androidx.annotation.StringRes
import com.mindInk.app.R
import com.mindInk.app.domain.model.AuthProvider
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.domain.repository.AuthErrorReason

data class AuthUiState(
    val loadingProvider: AuthProvider? = null
) {
    val isLoading: Boolean get() = loadingProvider != null
}

sealed interface SessionState {
    data object Loading : SessionState
    data object Unauthenticated : SessionState
    data class Authenticated(val user: AuthUser) : SessionState
}

@StringRes
fun AuthErrorReason.toMessageRes(): Int = when (this) {
    AuthErrorReason.NETWORK -> R.string.auth_error_network
    AuthErrorReason.NO_GOOGLE_ACCOUNT -> R.string.auth_error_no_google_account
    AuthErrorReason.ACCOUNT_COLLISION -> R.string.auth_error_account_collision
    AuthErrorReason.INVALID_CREDENTIAL -> R.string.auth_error_invalid_credential
    AuthErrorReason.UNKNOWN -> R.string.auth_error_unknown
}
