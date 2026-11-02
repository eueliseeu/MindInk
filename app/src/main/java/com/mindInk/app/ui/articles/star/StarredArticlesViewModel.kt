package com.mindInk.app.ui.articles.stars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.domain.usecase.auth.ObserveAuthStateUseCase
import com.mindInk.app.domain.usecase.star.ObserveStarredArticleIdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StarredArticlesViewModel @Inject constructor(
    observeAuthState: ObserveAuthStateUseCase,
    observeStarredArticleIds: ObserveStarredArticleIdsUseCase
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val starredIds: StateFlow<Set<String>> = observeAuthState()
        .map { user -> user?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptySet()) else observeStarredArticleIds(uid)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )
}