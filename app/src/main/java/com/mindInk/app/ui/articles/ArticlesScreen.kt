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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.R
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.articles.components.LatestArticlesSection
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkWhite

@Composable
fun ArticlesScreen(
    user: AuthUser,
    onSignOut: () -> Unit,
    onSearchClick: () -> Unit = {},
    onAddArticleClick: () -> Unit = {},
    onArticleClick: (ArticleCardUi) -> Unit = {},
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val navigationBarPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MindInkTopBar(
            screenTitle = stringResource(R.string.screen_articles),
            user = user,
            onSearchClick = onSearchClick,
            onSignOut = onSignOut
        )

        if (state.isLoading) {
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
                        articles = state.articles,
                        onArticleClick = onArticleClick,
                        onAddClick = onAddArticleClick
                    )
                }

                if (state.articles.isEmpty()) {
                    item(key = "empty_articles") {
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
            }
        }
    }
}
