package com.mindInk.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.repository.LoadAccountSettingsResult
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.usecase.article.ObserveArticlesByAuthorUseCase
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
    private val observeArticlesByAuthor: ObserveArticlesByAuthorUseCase,
    private val loadAccountSettings: LoadAccountSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthorProfileUiState())

    val uiState: StateFlow<AuthorProfileUiState> = _uiState.asStateFlow()

    private var articlesJob: Job? = null
    private var profileJob: Job? = null
    private var currentAuthorUid: String = ""
    private var hasProfileName = false

    fun load(authorUid: String) {
        if (authorUid.isBlank()) return
        if (currentAuthorUid == authorUid && articlesJob?.isActive == true) return

        currentAuthorUid = authorUid
        hasProfileName = false
        articlesJob?.cancel()
        profileJob?.cancel()

        _uiState.value = AuthorProfileUiState(authorUid = authorUid)

        profileJob = viewModelScope.launch {
            val result = loadAccountSettings(authorUid)
            if (result !is LoadAccountSettingsResult.Success || currentAuthorUid != authorUid) {
                return@launch
            }

            val profile = result.settings
            hasProfileName = profile.name.isNotBlank()
            _uiState.update { state ->
                state.copy(
                    authorName = if (hasProfileName) {
                        formatAuthorName(profile.name)
                    } else {
                        state.authorName
                    },
                    authorPhotoUrl = profile.photoUrl ?: state.authorPhotoUrl,
                    githubUrl = profile.github,
                    linkedinUrl = profile.linkedin
                )
            }
        }

        articlesJob = viewModelScope.launch {
            observeArticlesByAuthor(authorUid).collect { result ->
                when (result) {
                    is ObserveArticlesResult.Success -> {
                        val list = result.articles
                        val first = list.firstOrNull()
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                authorName = if (hasProfileName) {
                                    state.authorName
                                } else {
                                    formatAuthorName(first?.authorName)
                                },
                                authorPhotoUrl = state.authorPhotoUrl ?: first?.authorPhotoUrl,
                                totalStars = list.sumOf { it.starCount },
                                articles = list.map { it.toCardUi() },
                                errorMessage = null
                            )
                        }
                    }
                    is ObserveArticlesResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = result.message)
                        }
                    }
                }
            }
        }
    }
}