package com.mindInk.app.data.auth

import android.app.Activity
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.mindInk.app.domain.model.AuthProvider
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.domain.repository.AuthErrorReason
import com.mindInk.app.domain.repository.AuthRepository
import com.mindInk.app.domain.repository.SignInResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

private const val GITHUB_PROVIDER_ID = "github.com"
private const val GITHUB_EMAIL_SCOPE = "user:email"

internal class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleCredentialProvider: GoogleCredentialProvider
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override suspend fun signInWithGoogle(activity: Activity): SignInResult =
        when (val token = googleCredentialProvider.getIdToken(activity)) {
            is GoogleTokenResult.Token -> firebaseSignIn(AuthProvider.GOOGLE) {
                val credential = GoogleAuthProvider.getCredential(token.idToken, null)
                firebaseAuth.signInWithCredential(credential).awaitTask()
            }

            GoogleTokenResult.Cancelled -> SignInResult.Cancelled
            is GoogleTokenResult.Error -> SignInResult.Error(token.reason)
        }

    override suspend fun signInWithGitHub(activity: Activity): SignInResult =
        firebaseSignIn(AuthProvider.GITHUB) {
            val provider = OAuthProvider.newBuilder(GITHUB_PROVIDER_ID)
                .setScopes(listOf(GITHUB_EMAIL_SCOPE))
                .build()

            val pending = firebaseAuth.pendingAuthResult
            (pending ?: firebaseAuth.startActivityForSignInWithProvider(activity, provider))
                .awaitTask()
        }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        googleCredentialProvider.clearState()
    }

    private suspend fun firebaseSignIn(
        provider: AuthProvider,
        block: suspend () -> AuthResult
    ): SignInResult = try {
        val result = block()
        val user = result.user
        if (user != null) {
            val authUser = user.toAuthUser(
                provider = provider,
                fallbackName = result.additionalUserInfo?.username
            )
            SignInResult.Success(authUser)
        } else {
            SignInResult.Error(AuthErrorReason.UNKNOWN)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.toSignInResult()
    }
}
