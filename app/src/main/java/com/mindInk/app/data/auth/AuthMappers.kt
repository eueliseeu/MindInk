package com.mindInk.app.data.auth

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.mindInk.app.domain.model.AuthProvider
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.domain.repository.AuthErrorReason
import com.mindInk.app.domain.repository.SignInResult

private const val TAG = "AuthMappers"
private const val GOOGLE_PROVIDER_ID = "google.com"
private const val GITHUB_PROVIDER_ID = "github.com"
private const val GOOGLE_PHOTO_HOST = "googleusercontent.com"
private const val GOOGLE_PHOTO_SIZE = "=s400-c"

private val GOOGLE_PHOTO_SIZE_REGEX = Regex("=s\\d+-c$")

internal fun FirebaseUser.toAuthUser(
    provider: AuthProvider = resolveProvider(),
    fallbackName: String? = null
): AuthUser = AuthUser(
    uid = uid,
    email = email.orNullIfBlank()
        ?: providerData.firstNotNullOfOrNull { it.email.orNullIfBlank() },
    provider = provider,
    displayName = displayName.orNullIfBlank()
        ?: providerData.firstNotNullOfOrNull { it.displayName.orNullIfBlank() }
        ?: fallbackName.orNullIfBlank(),
    photoUrl = (photoUrl?.toString() ?: providerData.firstNotNullOfOrNull { it.photoUrl?.toString() })
        .orNullIfBlank()
        ?.toHighResPhoto()
)

private fun FirebaseUser.resolveProvider(): AuthProvider =
    providerData.firstNotNullOfOrNull { info ->
        when (info.providerId) {
            GOOGLE_PROVIDER_ID -> AuthProvider.GOOGLE
            GITHUB_PROVIDER_ID -> AuthProvider.GITHUB
            else -> null
        }
    } ?: AuthProvider.UNKNOWN

private fun String?.orNullIfBlank(): String? = this?.takeIf { it.isNotBlank() }

private fun String.toHighResPhoto(): String =
    if (contains(GOOGLE_PHOTO_HOST)) replace(GOOGLE_PHOTO_SIZE_REGEX, GOOGLE_PHOTO_SIZE) else this

internal fun Exception.toSignInResult(): SignInResult = when {
    this is FirebaseNetworkException ->
        SignInResult.Error(AuthErrorReason.NETWORK)

    this is FirebaseAuthUserCollisionException ->
        SignInResult.Error(AuthErrorReason.ACCOUNT_COLLISION)

    this is FirebaseAuthInvalidCredentialsException ->
        SignInResult.Error(AuthErrorReason.INVALID_CREDENTIAL)

    this is FirebaseAuthException && normalizedCode().contains("WEB_CONTEXT_CANCELED") ->
        SignInResult.Cancelled

    else -> {
        Log.w(TAG, "Falha inesperada na autenticação", this)
        SignInResult.Error(AuthErrorReason.UNKNOWN)
    }
}

internal fun Exception.isProviderAlreadyLinked(): Boolean =
    this is FirebaseAuthException && normalizedCode().contains("PROVIDER_ALREADY_LINKED")

private fun FirebaseAuthException.normalizedCode(): String =
    errorCode.uppercase().replace('-', '_')
