package com.mindInk.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.theme.MindInkBackground
import com.mindInk.app.ui.theme.MindInkSurface
import com.mindInk.app.ui.theme.MindInkSurfaceVariant
import com.mindInk.app.ui.theme.MindInkWhite

private val HorizontalPad = 20.dp

@Composable
fun GuestHomeScreen(
    onMyAccountClick: () -> Unit,
    onArticleClick: (ArticleCardUi) -> Unit,
    onAuthorClick: (AuthorUi) -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

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

            item(key = "famous_title") {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Famosos do MindInk",
                    color = MindInkWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = HorizontalPad)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            item(key = "famous_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = HorizontalPad),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = state.famousAuthors,
                        key = { it.uid }
                    ) { author ->
                        UserAvatar(
                            photoUrl = author.photoUrl,
                            name = author.name,
                            size = 56.dp,
                            modifier = Modifier.clickable { onAuthorClick(author) }
                        )
                    }
                }
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

            items(
                items = state.topRatedArticles,
                key = { "top_${it.id}" }
            ) { article ->
                ExploreArticleRow(
                    article = article,
                    currentUid = null,
                    onClick = { onArticleClick(article) }
                )
            }
        }

        MyAccountButton(
            onClick = onMyAccountClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPad, vertical = 12.dp)
        )
    }
}

@Composable
private fun MyAccountButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(MindInkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Minha Conta",
            color = MindInkWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MindInkWhite
        )
    }
}
