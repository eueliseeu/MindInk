package com.mindInk.app.ui.articles.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mikepenz.markdown.m3.Markdown
import com.mindInk.app.core.ArticleTextToSpeech
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.core.markdownToSpeechText
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.components.ToastHost
import com.mindInk.app.ui.components.ToastMessage
import com.mindInk.app.ui.theme.MindInkDim
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkStar
import com.mindInk.app.ui.theme.MindInkWhite
import kotlinx.coroutines.delay

private val ContentHorizontalPadding = 20.dp

@Composable
fun ArticleDetailScreen(
    articleId: String,
    user: AuthUser,
    onBack: () -> Unit,
    onSignOut: () -> Unit = {},
    onProfileClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val article = state.article
    val context = LocalContext.current
    var isSpeaking by remember { mutableStateOf(false) }
    val tts = remember {
        ArticleTextToSpeech(context) { speaking -> isSpeaking = speaking }
    }

    DisposableEffect(Unit) {
        onDispose { tts.shutdown() }
    }

    DisposableEffect(viewModel) {
        viewModel.onScreenVisible(true)
        onDispose { viewModel.onScreenVisible(false) }
    }

    LaunchedEffect(articleId) {
        viewModel.setArticleId(articleId)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.load()
            if (event == Lifecycle.Event.ON_PAUSE) tts.stop()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.toastMessage) {
        if (state.toastMessage != null) {
            delay(2500)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MindInkTopBar(
                screenTitle = "/ Lendo o Art.",
                user = user,
                onBack = {
                    tts.stop()
                    onBack()
                },
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

                article == null -> {
                    Text(
                        text = state.errorMessage ?: "Artigo não encontrado.",
                        color = MindInkMuted,
                        modifier = Modifier.padding(
                            horizontal = ContentHorizontalPadding,
                            vertical = 20.dp
                        )
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = ContentHorizontalPadding)
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = "Escritor: ${formatAuthorName(article.authorName)}",
                            color = MindInkMuted,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = article.title,
                            color = MindInkWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 30.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (article.subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = article.subtitle,
                                color = MindInkMuted,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (article.category.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = article.category,
                                color = MindInkDim,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Markdown(
                            content = article.content,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    interactionSource = remember(article.id) {
                                        MutableInteractionSource()
                                    },
                                    indication = null,
                                    onClick = {},
                                    onDoubleClick = viewModel::onToggleStar
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clickable(
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        },
                                        indication = null,
                                        role = Role.Button
                                    ) {
                                        if (isSpeaking) {
                                            tts.stop()
                                        } else {
                                            tts.speak(
                                                markdownToSpeechText(
                                                    title = article.title,
                                                    subtitle = article.subtitle,
                                                    content = article.content
                                                )
                                            )
                                        }
                                    }
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) {
                                        Icons.Rounded.Stop
                                    } else {
                                        Icons.Rounded.VolumeUp
                                    },
                                    contentDescription = null,
                                    tint = MindInkWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSpeaking) "Parar leitura" else "Ouvir artigo",
                                    color = MindInkWhite,
                                    fontSize = 13.sp
                                )
                            }

                            IconButton(
                                onClick = viewModel::onToggleStar,
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (state.isStarredByMe) {
                                            Icons.Rounded.Star
                                        } else {
                                            Icons.Outlined.Star
                                        },
                                        contentDescription = "Curtir artigo",
                                        tint = if (state.isStarredByMe) {
                                            MindInkStar
                                        } else {
                                            MindInkWhite
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = article.starCount.toString(),
                                        color = MindInkWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        ToastHost(
            message = state.toastMessage?.let { ToastMessage(it) },
            onDismiss = viewModel::clearToast
        )
    }
}
