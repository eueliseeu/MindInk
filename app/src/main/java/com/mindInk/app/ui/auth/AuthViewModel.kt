package com.mindInk.app.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.domain.model.AuthProvider
import com.mindInk.app.domain.repository.AuthErrorReason
import com.mindInk.app.domain.repository.SignInResult
import com.mindInk.app.domain.usecase.auth.ObserveAuthStateUseCase
import com.mindInk.app.domain.usecase.auth.SignInWithGitHubUseCase
import com.mindInk.app.domain.usecase.auth.SignInWithGoogleUseCase
import com.mindInk.app.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInWithGitHubUseCase: SignInWithGitHubUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = observeAuthStateUseCase()
        .map { user ->
            if (user != null) SessionState.Authenticated(user) else SessionState.Unauthenticated
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionState.Loading
        )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _errorEvents = Channel<AuthErrorReason>(Channel.BUFFERED)
    val errorEvents: Flow<AuthErrorReason> = _errorEvents.receiveAsFlow()

    fun onGoogleClick(activity: Activity) {
        launchSignIn(AuthProvider.GOOGLE) { signInWithGoogleUseCase(activity) }
    }

    fun onGitHubClick(activity: Activity) {
        launchSignIn(AuthProvider.GITHUB) { signInWithGitHubUseCase(activity) }
    }

    fun onSignOutClick() {
        viewModelScope.launch { signOutUseCase() }
    }

    private fun launchSignIn(provider: AuthProvider, block: suspend () -> SignInResult) {
        if (_uiState.value.isLoading) return
        _uiState.value = AuthUiState(loadingProvider = provider)

        viewModelScope.launch {
            val result = block()
            _uiState.value = AuthUiState()

            if (result is SignInResult.Error) {
                _errorEvents.send(result.reason)
            }
        }
    }
}
