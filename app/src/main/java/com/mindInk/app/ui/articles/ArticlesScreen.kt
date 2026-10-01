package com.mindInk.app.ui.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mindInk.app.R
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.articles.components.LatestArticlesSection
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.components.MindInkTopBar

@Composable
fun ArticlesScreen(
    user: AuthUser,
    onSignOut: () -> Unit,
    onSearchClick: () -> Unit = {},
    onAddArticleClick: () -> Unit = {},
    onArticleClick: (ArticleCardUi) -> Unit = {}
) {
    val latestArticles = remember { articlePlaceholders() }
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

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

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp + navigationBarPadding)
        ) {
            item(key = "latest_articles") {
                LatestArticlesSection(
                    articles = latestArticles,
                    onArticleClick = onArticleClick,
                    onAddClick = onAddArticleClick
                )
            }
        }
    }
}
