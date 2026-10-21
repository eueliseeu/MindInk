package com.mindInk.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.repository.LoadAccountSettingsResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.usecase.article.ObserveArticlesUseCase
import com.mindInk.app.domain.usecase.settings.LoadAccountSettingsUseCase
import com.mindInk.app.ui.articles.mapper.toCardUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthorProfileViewModel @Inject constructor(
    private val observeArticles: ObserveArticlesUseCase,
    private val loadAccountSettings: LoadAccountSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthorProfileUiState())
    val uiState: StateFlow<AuthorProfileUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var settingsJob: Job? = null
    private var currentAuthorUid: String = ""

    fun load(authorUid: String) {
        if (authorUid.isBlank()) return

        if (currentAuthorUid == authorUid && observeJob?.isActive == true) {
            return
        }

        currentAuthorUid = authorUid
        observeJob?.cancel()
        settingsJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                authorUid = authorUid,
                githubUrl = "",
                linkedinUrl = "",
                errorMessage = null
            )
        }

        settingsJob = viewModelScope.launch {
            when (val result = loadAccountSettings(authorUid)) {
                is LoadAccountSettingsResult.Success -> {
                    _uiState.update { state ->
                        if (state.authorUid != authorUid) state else state.copy(
                            githubUrl = result.settings.github,
                            linkedinUrl = result.settings.linkedin
                        )
                    }
                }
                is LoadAccountSettingsResult.Error -> Unit
            }
        }

        observeJob = viewModelScope.launch {
            observeArticles().collect { result ->
                when (result) {
                    is ObserveArticlesResult.Success -> {
                        val list = result.articles.filter { it.authorUid == currentAuthorUid }
                        val first = list.firstOrNull()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                authorName = formatAuthorName(first?.authorName)
                                    .ifBlank { it.authorName },
                                authorUsername = first?.authorUsername
                                    ?.takeIf { username -> username.isNotBlank() }
                                    ?: it.authorUsername,
                                authorPhotoUrl = first?.authorPhotoUrl ?: it.authorPhotoUrl,
                                totalStars = list.sumOf { article -> article.starCount },
                                articles = list.map { article -> article.toCardUi() },
                                errorMessage = null
                            )
                        }
                    }
                    is ObserveArticlesResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        observeJob?.cancel()
        settingsJob?.cancel()
        super.onCleared()
    }
}
