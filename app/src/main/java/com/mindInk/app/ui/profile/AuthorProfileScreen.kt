package com.mindInk.app.ui.profile

import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.R
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.articles.components.ExploreArticleRow
import com.mindInk.app.ui.articles.formatCount
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkSurface
import com.mindInk.app.ui.theme.MindInkWhite

@Composable
fun AuthorProfileScreen(
    authorUid: String,
    user: AuthUser,
    onBack: () -> Unit,
    onArticleClick: (ArticleCardUi) -> Unit,
    onSignOut: () -> Unit = {},
    onProfileClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    viewModel: AuthorProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(authorUid) {
        viewModel.load(authorUid)
    }

    val titleName = state.authorName.ifBlank { "Autor" }
    val firstName = titleName.substringBefore(" ").ifBlank { titleName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        MindInkTopBar(
            screenTitle = "/ $firstName",
            user = user,
            onBack = onBack,
            showSearch = false,
            showUserMenu = true,
            onSignOut = onSignOut,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick
        )

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
                    modifier = Modifier.padding(20.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item(key = "header") {
                        ProfileHeader(
                            name = state.authorName,
                            photoUrl = state.authorPhotoUrl,
                            githubUrl = state.githubUrl,
                            linkedinUrl = state.linkedinUrl,
                            totalStars = state.totalStars
                        )
                    }

                    item(key = "section_title") {
                        Text(
                            text = "Artigos de ${state.authorName}",
                            color = MindInkWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 28.dp,
                                bottom = 12.dp
                            )
                        )
                    }

                    if (state.articles.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = "Nenhum artigo publicado.",
                                color = MindInkMuted,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    } else {
                        items(
                            items = state.articles,
                            key = { article -> article.id }
                        ) { article ->
                            ExploreArticleRow(
                                article = article,
                                currentUid = user.uid,
                                onClick = { onArticleClick(article) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    photoUrl: String?,
    githubUrl: String,
    linkedinUrl: String,
    totalStars: Int
) {
    val avatarSize = 128.dp
    val bandY = 88.dp
    val uriHandler = LocalUriHandler.current
    val githubLink = socialLink(githubUrl, "github.com")
    val linkedinLink = socialLink(linkedinUrl, "linkedin.com")

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bandY + avatarSize / 2 + 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(bandY)
                    .background(MaterialTheme.colorScheme.background)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(avatarSize / 2 + 8.dp)
                    .background(MindInkSurface)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = bandY)
                    .height(1.dp)
                    .background(MindInkBorder.copy(alpha = 0.45f))
            )

            UserAvatar(
                photoUrl = photoUrl,
                name = name,
                size = avatarSize,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = bandY - avatarSize / 2)
                    .zIndex(2f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MindInkSurface)
                .padding(top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    text = "Escritor",
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (githubLink != null) {
                        IconButton(
                            onClick = { uriHandler.openUri(githubLink) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_mindink_github),
                                contentDescription = "Abrir GitHub",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (linkedinLink != null) {
                        IconButton(
                            onClick = { uriHandler.openUri(linkedinLink) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = "Abrir LinkedIn",
                                tint = MindInkWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MindInkWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatCount(totalStars),
                    color = MindInkWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun socialLink(value: String, host: String): String? {
    val trimmed = value.trim().removePrefix("@")
    if (trimmed.isBlank()) return null

    val address = when {
        trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true) -> trimmed
        trimmed.startsWith(host, ignoreCase = true) ||
            trimmed.startsWith("www.$host", ignoreCase = true) -> "https://$trimmed"
        else -> "https://$host/${trimmed.trimStart('/')}"
    }
    val uri = Uri.parse(address)
    return address.takeIf { uri.scheme == "https" || uri.scheme == "http" }
}
