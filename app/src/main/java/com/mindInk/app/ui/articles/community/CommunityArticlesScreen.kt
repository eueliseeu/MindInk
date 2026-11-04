package com.mindInk.app.ui.articles.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.model.ArticleCategories
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.articles.ArticlesViewModel
import com.mindInk.app.ui.articles.components.ExploreArticleRow
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.theme.MindInkBackground
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkSurface
import com.mindInk.app.ui.theme.MindInkWhite
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private val FilterOptions = listOf("Comunidade") + ArticleCategories.ALL
private const val SCROLL_TOP_THRESHOLD = 8

@Composable
fun CommunityArticlesScreen(
    user: AuthUser,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onProfileClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onArticleClick: (ArticleCardUi) -> Unit,
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val navigationBarPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var selectedFilter by remember { mutableStateOf("Comunidade") }
    var searchQuery by remember { mutableStateOf("") }
    var filterExpanded by remember { mutableStateOf(false) }

    val displayName = formatAuthorName(user.displayName ?: user.email)
        .trim()
        .split(" ")
        .first()

    val filteredArticles by remember(state.exploreArticles, selectedFilter, searchQuery) {
        derivedStateOf {
            state.exploreArticles
                .asSequence()
                .filter { article ->
                    selectedFilter == "Comunidade" ||
                        article.category.equals(selectedFilter, ignoreCase = true)
                }
                .filter { article ->
                    val q = searchQuery.trim()
                    if (q.isEmpty()) return@filter true
                    article.title.contains(q, ignoreCase = true) ||
                        article.summary.contains(q, ignoreCase = true) ||
                        article.authorName.contains(q, ignoreCase = true)
                }
                .toList()
        }
    }

    val showScrollTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex >= SCROLL_TOP_THRESHOLD
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && state.hasMoreExplore && !state.isLoadingMore) {
                    viewModel.loadMoreExplore()
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindInkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MindInkTopBar(
                screenTitle = "MindInk.",
                user = user,
                showSearch = false,
                onSignOut = onSignOut,
                onProfileClick = onProfileClick,
                onSettingsClick = onSettingsClick
            )

            if (state.isLoading && state.exploreArticles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                        top = 4.dp,
                        bottom = 88.dp + navigationBarPadding
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
                                text = "Olá, $displayName",
                                color = MindInkWhite,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (onSettingsClick != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { onSettingsClick() }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Minhas Configurações",
                                        color = MindInkMuted,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MindInkMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    item(key = "filter") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(MindInkWhite)
                                    .clickable { filterExpanded = true }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedFilter,
                                    color = MindInkBackground,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MindInkBackground,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = filterExpanded,
                                onDismissRequest = { filterExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(MindInkSurface)
                            ) {
                                FilterOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                color = MindInkWhite,
                                                fontSize = 15.sp
                                            )
                                        },
                                        onClick = {
                                            selectedFilter = option
                                            filterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item(key = "search") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .border(1.dp, MindInkBorder, RoundedCornerShape(28.dp))
                                .background(MindInkSurface)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                cursorBrush = SolidColor(MindInkWhite),
                                textStyle = TextStyle(
                                    color = MindInkWhite,
                                    fontSize = 15.sp
                                ),
                                modifier = Modifier.weight(1f),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Buscar artigos",
                                            color = MindInkMuted,
                                            fontSize = 15.sp
                                        )
                                    }
                                    inner()
                                }
                            )
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MindInkMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    item(key = "section_title") {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Artigos",
                            color = MindInkWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (filteredArticles.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = "Nenhum artigo encontrado.",
                                color = MindInkMuted,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(
                                    horizontal = 20.dp,
                                    vertical = 12.dp
                                )
                            )
                        }
                    } else {
                        items(
                            items = filteredArticles,
                            key = { it.id }
                        ) { article ->
                            ExploreArticleRow(
                                article = article,
                                currentUid = user.uid,
                                onClick = { onArticleClick(article) }
                            )
                        }
                    }

                    if (state.isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MindInkWhite,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showScrollTop,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .padding(bottom = navigationBarPadding)
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                shape = CircleShape,
                containerColor = MindInkWhite,
                contentColor = MindInkBackground,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Voltar ao topo",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}