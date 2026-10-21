package com.mindInk.app.ui.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.R
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.articles.components.AuthorsSection
import com.mindInk.app.ui.articles.components.ExploreArticleRow
import com.mindInk.app.ui.articles.components.LatestArticlesSection
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.model.AuthorUi
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkWhite
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ArticlesScreen(
    user: AuthUser,
    onSignOut: () -> Unit,
    onProfileClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onSearchClick: () -> Unit = {},
    onAddArticleClick: () -> Unit = {},
    onArticleClick: (ArticleCardUi) -> Unit = {},
    onAuthorClick: (AuthorUi) -> Unit = {},
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val navigationBarPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(listState, state.hasMoreExplore, state.isLoadingMore) {
        snapshotFlow {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && state.hasMoreExplore && !state.isLoadingMore) {
                    viewModel.loadMoreExplore()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MindInkTopBar(
            screenTitle = stringResource(R.string.screen_articles),
            user = user,
            onSearchClick = onSearchClick,
            onSignOut = onSignOut,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick
        )

        if (state.isLoading && state.latestArticles.isEmpty() && state.exploreArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MindInkWhite)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    top = 24.dp,
                    bottom = 24.dp + navigationBarPadding
                )
            ) {
                item(key = "latest_articles") {
                    LatestArticlesSection(
                        articles = state.latestArticles,
                        currentUid = user.uid,
                        onArticleClick = onArticleClick,
                        onAddClick = onAddArticleClick
                    )
                }

                if (state.latestArticles.isEmpty()) {
                    item(key = "empty_latest") {
                        Text(
                            text = state.errorMessage
                                ?: "Nenhum artigo ainda. Toque em + para publicar.",
                            color = MindInkMuted,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(
                                horizontal = 20.dp,
                                vertical = 24.dp
                            )
                        )
                    }
                }

                if (state.authors.isNotEmpty()) {
                    item(key = "authors") {
                        AuthorsSection(
                            authors = state.authors,
                            onAuthorClick = onAuthorClick,
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    }
                }

                if (state.exploreArticles.isNotEmpty()) {
                    item(key = "explore_title") {
                        Text(
                            text = "Descubra Mais Artigos",
                            color = MindInkWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 32.dp,
                                bottom = 16.dp
                            )
                        )
                    }

                    items(
                        items = state.exploreArticles,
                        key = { "explore_${it.id}" }
                    ) { article ->
                        ExploreArticleRow(
                            article = article,
                            currentUid = user.uid,
                            onClick = { onArticleClick(article) }
                        )
                    }
                }

                if (state.isLoadingMore) {
                    item(key = "explore_loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MindInkWhite,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                if (!state.hasMoreExplore && state.exploreArticles.isNotEmpty()) {
                    item(key = "explore_end") {
                        Text(
                            text = "Você viu todos os artigos.",
                            color = MindInkMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(
                                horizontal = 20.dp,
                                vertical = 16.dp
                            )
                        )
                    }
                }
            }
        }
    }
}
