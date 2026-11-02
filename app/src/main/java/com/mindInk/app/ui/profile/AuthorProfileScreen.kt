package com.mindInk.app.ui.profile

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Visibility
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
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.articles.components.ExploreArticleRow
import com.mindInk.app.ui.articles.formatCount
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.theme.MindInkBackground
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkStar
import com.mindInk.app.ui.theme.MindInkWhite

private val BandHeight = 220.dp
private val AvatarSize = 96.dp

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
    val isOwnProfile = authorUid == user.uid

    LaunchedEffect(authorUid) {
        viewModel.load(authorUid)
    }

    // O ViewModel é reaproveitado entre perfis: até o load() rodar, o estado ainda
    // pertence ao perfil anterior e não pode ser exibido.
    val isLoading = state.isLoading || state.authorUid != authorUid

    val displayName = state.authorName.ifBlank {
        if (isOwnProfile) {
            user.displayName?.takeIf { it.isNotBlank() } ?: "Você"
        } else {
            "Autor"
        }
    }
    val shortName = displayName.substringBefore(" ").ifBlank { displayName }
    val totalViews = state.articles.sumOf { it.viewCount }
    val totalStars = state.articles.sumOf { it.starCount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MindInkWhite)
                }
            }

            state.errorMessage != null && state.articles.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(20.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MindInkWhite
                        )
                    }
                    Text(
                        text = state.errorMessage ?: "Erro",
                        color = MindInkMuted
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item(key = "header") {
                        StandardProfileHeader(
                            name = displayName,
                            photoUrl = state.authorPhotoUrl ?: user.photoUrl.takeIf { isOwnProfile },
                            githubUrl = state.githubUrl,
                            linkedinUrl = state.linkedinUrl,
                            isOwnProfile = isOwnProfile,
                            onBack = onBack
                        )
                    }

                    item(key = "section_title") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOwnProfile) {
                                    "Meus artigos"
                                } else {
                                    "Artigos de $shortName"
                                },
                                color = MindInkWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = MindInkMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatCount(totalViews),
                                        color = MindInkMuted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Star,
                                        contentDescription = null,
                                        tint = MindInkStar,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatCount(totalStars),
                                        color = MindInkMuted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
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
fun StandardProfileHeader(
    name: String,
    photoUrl: String?,
    githubUrl: String,
    linkedinUrl: String,
    isOwnProfile: Boolean = false,
    onBack: (() -> Unit)? = null
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

            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 8.dp, top = 4.dp)
                        .align(Alignment.TopStart)
                        .zIndex(3f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MindInkWhite
                    )
                }
            }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    color = MindInkWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isOwnProfile) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(me)",
                        color = MindInkMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

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
                             Image(
                                painter = painterResource(R.drawable.linkedin),
                                contentDescription = "Linkedin",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}