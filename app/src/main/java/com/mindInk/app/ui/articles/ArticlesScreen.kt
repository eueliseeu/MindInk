package com.mindInk.app.ui.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.R
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.articles.components.ContinueReadingSection
import com.mindInk.app.ui.articles.components.MuralSection
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.model.AuthorUi
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkSurfaceVariant
import com.mindInk.app.ui.theme.MindInkWhite

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

    val displayName = formatAuthorName(user.displayName ?: user.email)
        .trim()
        .split(" ")
        .first()

    LaunchedEffect(user.uid) {
        viewModel.bindUser(user.uid)
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
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 24.dp + navigationBarPadding
                )
            ) {
                item(key = "greeting") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Bem-vindo(a) de volta",
                            color = MindInkMuted,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Olá, $displayName!",
                            color = MindInkWhite,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item(key = "mural") {
                    Spacer(modifier = Modifier.height(20.dp))

                    MuralSection(
                        items = state.muralItems,
                        onNewArticleClick = { item ->
                            onArticleClick(
                                ArticleCardUi(
                                    id = item.articleId,
                                    category = "",
                                    title = item.articleTitle,
                                    summary = "",
                                    authorName = item.actorName,
                                    authorPhotoUrl = item.actorPhotoUrl,
                                    starCount = 0,
                                    viewCount = 0,
                                    starredBy = emptyList(),
                                    createdAt = item.sortKey
                                )
                            )
                        }
                    )
                }

                item(key = "artigos_button") {
                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable { onAddArticleClick() },
                        shape = RoundedCornerShape(28.dp),
                        color = MindInkSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 14.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Artigos",
                                color = MindInkWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MindInkWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                item(key = "continue_reading") {
                    Spacer(modifier = Modifier.height(28.dp))

                    ContinueReadingSection(
                        articles = state.continueReading,
                        currentUid = user.uid,
                        onArticleClick = onArticleClick
                    )
                }
            }
        }
    }
}