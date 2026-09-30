package com.mindInk.app.data.auth

import android.app.Activity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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

    private sealed interface PendingLink {
        val provider: AuthProvider

        data class Google(val credential: AuthCredential) : PendingLink {
            override val provider: AuthProvider get() = AuthProvider.GOOGLE
        }

        data object GitHub : PendingLink {
            override val provider: AuthProvider get() = AuthProvider.GITHUB
        }
    }

    @Volatile
    private var pendingLink: PendingLink? = null

    override fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override suspend fun signInWithGoogle(activity: Activity): SignInResult =
        googleSignIn(activity, linkOnCollision = true)

    override suspend fun signInWithGitHub(activity: Activity): SignInResult =
        gitHubSignIn(activity, linkOnCollision = true)

    override suspend fun linkPendingAccount(activity: Activity): SignInResult {
        val pending = pendingLink ?: return SignInResult.Error(AuthErrorReason.UNKNOWN)
        pendingLink = null

        val signIn = when (pending) {
            is PendingLink.Google -> gitHubSignIn(activity, linkOnCollision = false)
            PendingLink.GitHub -> googleSignIn(activity, linkOnCollision = false)
        }
        if (signIn !is SignInResult.Success) return signIn

        val user = firebaseAuth.currentUser
            ?: return SignInResult.Error(AuthErrorReason.UNKNOWN)

        return try {
            when (pending) {
                is PendingLink.Google ->
                    user.linkWithCredential(pending.credential).awaitTask()

                PendingLink.GitHub ->
                    user.startActivityForLinkWithProvider(activity, gitHubProvider()).awaitTask()
            }
            val linkedUser = firebaseAuth.currentUser ?: user
            SignInResult.Success(linkedUser.toAuthUser(pending.provider))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e.isProviderAlreadyLinked()) signIn else e.toSignInResult()
        }
    }

    override fun discardPendingLink() {
        pendingLink = null
    }

    override suspend fun signOut() {
        pendingLink = null
        firebaseAuth.signOut()
        googleCredentialProvider.clearState()
    }

    private suspend fun googleSignIn(activity: Activity, linkOnCollision: Boolean): SignInResult =
        when (val token = googleCredentialProvider.getIdToken(activity)) {
            is GoogleTokenResult.Token -> {
                val credential = GoogleAuthProvider.getCredential(token.idToken, null)
                val pendingOnCollision = if (linkOnCollision) PendingLink.Google(credential) else null
                firebaseSignIn(AuthProvider.GOOGLE, pendingOnCollision) {
                    firebaseAuth.signInWithCredential(credential).awaitTask()
                }
            }

            GoogleTokenResult.Cancelled -> SignInResult.Cancelled
            is GoogleTokenResult.Error -> SignInResult.Error(token.reason)
        }

    private suspend fun gitHubSignIn(activity: Activity, linkOnCollision: Boolean): SignInResult {
        val pendingOnCollision = if (linkOnCollision) PendingLink.GitHub else null
        return firebaseSignIn(AuthProvider.GITHUB, pendingOnCollision) {
            val pending = firebaseAuth.pendingAuthResult
            (pending ?: firebaseAuth.startActivityForSignInWithProvider(activity, gitHubProvider()))
                .awaitTask()
        }
    }

    private fun gitHubProvider(): OAuthProvider =
        OAuthProvider.newBuilder(GITHUB_PROVIDER_ID)
            .setScopes(listOf(GITHUB_EMAIL_SCOPE))
            .build()

    private suspend fun firebaseSignIn(
        provider: AuthProvider,
        pendingOnCollision: PendingLink?,
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
        if (pendingOnCollision != null && e is FirebaseAuthUserCollisionException) {
            pendingLink = pendingOnCollision
            SignInResult.LinkRequired(
                pendingProvider = provider,
                existingProvider = provider.counterpart()
            )
        } else {
            e.toSignInResult()
        }
    }

    private fun AuthProvider.counterpart(): AuthProvider = when (this) {
        AuthProvider.GOOGLE -> AuthProvider.GITHUB
        AuthProvider.GITHUB -> AuthProvider.GOOGLE
        AuthProvider.UNKNOWN -> AuthProvider.UNKNOWN
    }
}
