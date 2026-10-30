package com.mindInk.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.R
import com.mindInk.app.core.normalizeSocialProfileUrl
import com.mindInk.app.ui.articles.components.ExploreArticleRow
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.components.MindInkCtaButton
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.profile.AuthorProfileViewModel
import com.mindInk.app.ui.theme.MindInkBackground
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkWhite

private const val GUEST_ARTICLES_LIMIT = 2
private val BandHeight = 220.dp
private val AvatarSize = 96.dp

@Composable
fun GuestAuthorProfileScreen(
    authorUid: String,
    onBack: () -> Unit,
    onArticleClick: (ArticleCardUi) -> Unit,
    onAccessMoreClick: () -> Unit,
    viewModel: AuthorProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(authorUid) {
        viewModel.load(authorUid)
    }

    val displayName = state.authorName.ifBlank { "Autor" }
    val shortName = displayName.substringBefore(" ").ifBlank { displayName }
    val previewArticles = state.articles.take(GUEST_ARTICLES_LIMIT)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MindInkWhite)
                }
            }

            state.errorMessage != null && state.articles.isEmpty() -> {
                Text(
                    text = state.errorMessage ?: "Erro",
                    color = MindInkMuted,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(20.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item(key = "header") {
                        GuestProfileHeader(
                            name = displayName,
                            photoUrl = state.authorPhotoUrl,
                            githubUrl = state.githubUrl,
                            linkedinUrl = state.linkedinUrl
                        )
                    }

                    item(key = "section_title") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Alguns artigos do $shortName.",
                            color = MindInkWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }

                    items(previewArticles, key = { it.id }) { article ->
                        ExploreArticleRow(
                            article = article,
                            currentUid = null,
                            onClick = { onArticleClick(article) }
                        )
                    }

                    item(key = "cta") {
                        Spacer(modifier = Modifier.height(20.dp))
                        MindInkCtaButton(
                            text = "Acesse para ver mais",
                            onClick = onAccessMoreClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestProfileHeader(
    name: String,
    photoUrl: String?,
    githubUrl: String,
    linkedinUrl: String
) {
    val uriHandler = LocalUriHandler.current
    val githubLink = normalizeSocialProfileUrl(githubUrl, "github")
    val linkedinLink = normalizeSocialProfileUrl(linkedinUrl, "linkedin")
    val overlap = AvatarSize / 2

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BandHeight + overlap)
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile_image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BandHeight)
                    .align(Alignment.TopCenter)
                    .zIndex(0f)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(overlap)
                    .align(Alignment.BottomCenter)
                    .background(MindInkBackground)
                    .zIndex(1f)
            )

            UserAvatar(
                photoUrl = photoUrl,
                name = name,
                size = AvatarSize,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp)
                    .zIndex(2f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Text(
                text = name,
                color = MindInkWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Escritor Verificado",
                    color = MindInkMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = MindInkWhite,
                    modifier = Modifier.size(14.dp)
                )
            }

            if (githubLink != null || linkedinLink != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (githubLink != null) {
                        IconButton(
                            onClick = { uriHandler.openUri(githubLink) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_mindink_github),
                                contentDescription = "GitHub",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (linkedinLink != null) {
                        IconButton(
                            onClick = { uriHandler.openUri(linkedinLink) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = "LinkedIn",
                                tint = MindInkWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}