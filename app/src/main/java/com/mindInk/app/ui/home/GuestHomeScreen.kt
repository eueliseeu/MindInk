package com.mindInk.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.R
import com.mindInk.app.ui.articles.ArticlesViewModel
import com.mindInk.app.ui.articles.components.ExploreArticleRow
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.model.AuthorUi
import com.mindInk.app.ui.components.MindInkCtaButton
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.theme.MindInkBackground
import com.mindInk.app.ui.theme.MindInkSurface
import com.mindInk.app.ui.theme.MindInkWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val HorizontalPad = 20.dp
private const val VISIBLE_COUNT = 4
private const val ROTATE_MS = 60_000L

@Composable
fun GuestHomeScreen(
    onMyAccountClick: () -> Unit,
    onArticleClick: (ArticleCardUi) -> Unit,
    onAuthorClick: (AuthorUi) -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val pool = state.topRatedArticles
    var visiblePair by remember { mutableStateOf<List<ArticleCardUi>>(emptyList()) }

    LaunchedEffect(pool) {
        if (pool.isEmpty()) {
            visiblePair = emptyList()
            return@LaunchedEffect
        }
        visiblePair = pickTwo(pool, visiblePair)
        while (isActive) {
            delay(ROTATE_MS)
            visiblePair = pickTwo(pool, visiblePair)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindInkBackground)
            .navigationBarsPadding()
    ) {
        MindInkTopBar(
            screenTitle = "/ Mindlink.",
            user = null,
            showSearch = true,
            showUserMenu = false,
            onSearchClick = onSearchClick
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item(key = "banner") {
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.home_banner),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HorizontalPad)
                        .height(140.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MindInkSurface)
                )
            }

            item(key = "top_title") {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Artigos mais avaliados",
                    color = MindInkWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = HorizontalPad)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item(key = "top_rated_pair") {
                AnimatedContent(
                    targetState = visiblePair,
                    transitionSpec = {
                        fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                    },
                    label = "guestTopRated"
                ) { pair ->
                    Column {
                        pair.take(VISIBLE_COUNT).forEach { article ->
                            ExploreArticleRow(
                                article = article,
                                currentUid = null,
                                onClick = { onArticleClick(article) }
                            )
                        }
                    }
                }
            }
        }

        MindInkCtaButton(
            text = "Minha Conta",
            onClick = onMyAccountClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPad, vertical = 12.dp)
        )
    }
}

private fun pickTwo(
    pool: List<ArticleCardUi>,
    current: List<ArticleCardUi>
): List<ArticleCardUi> {
    if (pool.isEmpty()) return emptyList()
    if (pool.size <= VISIBLE_COUNT) return pool.shuffled().take(VISIBLE_COUNT)
    val currentIds = current.map { it.id }.toSet()
    val preferred = pool.filter { it.id !in currentIds }
    val source = if (preferred.size >= VISIBLE_COUNT) preferred else pool
    return source.shuffled().take(VISIBLE_COUNT)
}