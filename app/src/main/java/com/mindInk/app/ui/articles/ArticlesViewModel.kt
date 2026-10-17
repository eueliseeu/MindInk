package com.mindInk.app.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.data.article.toCardUi
import com.mindInk.app.domain.repository.ObserveArticlesResult
import com.mindInk.app.domain.usecase.article.ObserveArticlesUseCase
import com.mindInk.app.ui.articles.model.AuthorUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val observeArticles: ObserveArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticlesUiState())
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeArticles().collect { result ->
                when (result) {
                    is ObserveArticlesResult.Success -> {
                        val authors = result.articles
                            .distinctBy { it.authorUid }
                            .map { article ->
                                AuthorUi(
                                    uid = article.authorUid,
                                    name = formatAuthorName(article.authorName),
                                    photoUrl = article.authorPhotoUrl
                                )
                            }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                articles = result.articles.map { article -> article.toCardUi() },
                                authors = authors,
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
}
