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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.model.ArticleCategories
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.articles.components.ExploreArticleRow
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.components.GreetingHeader
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.theme.MindInkBackground
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkSurface
import com.mindInk.app.ui.theme.MindInkWhite
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private val CategoryOptions = listOf<String?>(null) + ArticleCategories.ALL
private const val SCROLL_TOP_THRESHOLD = 8

@Composable
fun CommunityArticlesScreen(
    user: AuthUser,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onProfileClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onArticleClick: (ArticleCardUi) -> Unit,
    onCreateArticleClick: () -> Unit,
    viewModel: CommunityArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val navigationBarPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var filterExpanded by rememberSaveable { mutableStateOf(false) }

    val displayName = formatAuthorName(user.displayName ?: user.email)
        .trim()
        .split(" ")
        .first()

    val filterLabel = state.selectedCategory ?: "Comunidade"

    val showScrollTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex >= SCROLL_TOP_THRESHOLD }
    }

    LaunchedEffect(listState, state.hasMore, state.isLoadingMore, state.isLoading) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= (total - 3).coerceAtLeast(0)
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && state.hasMore && !state.isLoadingMore && !state.isLoading) {
                    viewModel.loadMore()
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
                onBack = onBack,
                showSearch = false,
                onSignOut = onSignOut,
                onProfileClick = onProfileClick,
                onSettingsClick = onSettingsClick
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = 100.dp + navigationBarPadding
                )
            ) {
                item(key = "greeting") {
                    GreetingHeader(firstName = displayName)
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
                                .clickable(
                                    role = Role.Button,
                                    onClick = { filterExpanded = true }
                                )
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = filterLabel,
                                color = MindInkBackground,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Abrir filtro de categoria",
                                tint = MindInkBackground,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false },
                            modifier = Modifier.background(MindInkSurface)
                        ) {
                            CategoryOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option ?: "Comunidade",
                                            color = MindInkWhite,
                                            fontSize = 15.sp
                                        )
                                    },
                                    onClick = {
                                        viewModel.onCategorySelected(option)
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
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            singleLine = true,
                            cursorBrush = SolidColor(MindInkWhite),
                            textStyle = TextStyle(
                                color = MindInkWhite,
                                fontSize = 15.sp
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            decorationBox = { inner ->
                                if (state.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Buscar artigos",
                                        color = MindInkMuted,
                                        fontSize = 15.sp
                                    )
                                }
                                inner()
                            }
                        )
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = viewModel::clearSearch,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Limpar busca",
                                    tint = MindInkMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Buscar",
                                tint = MindInkMuted,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                    if (state.searchQuery.isNotBlank()) {
                        Text(
                            text = "Busca nos artigos já carregados.",
                            color = MindInkMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(
                                horizontal = 24.dp,
                                vertical = 6.dp
                            )
                        )
                    }
                }

                item(key = "section_title") {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Artigos",
                        color = MindInkWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (state.isLoading) {
                    item(key = "list_loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MindInkWhite,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                } else {
                    val visible = state.visibleArticles
                    if (visible.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = state.errorMessage ?: "Nenhum artigo encontrado.",
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
                            items = visible,
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp + navigationBarPadding),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = showScrollTop,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch { listState.animateScrollToItem(0) }
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

            Spacer(modifier = Modifier.height(12.dp))

            FloatingActionButton(
                onClick = onCreateArticleClick,
                shape = CircleShape,
                containerColor = MindInkWhite,
                contentColor = MindInkBackground,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Criar artigo",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}