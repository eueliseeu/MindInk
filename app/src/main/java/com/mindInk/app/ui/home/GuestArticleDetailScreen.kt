package com.mindInk.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.ui.articles.detail.ArticleDetailViewModel
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkSurfaceVariant
import com.mindInk.app.ui.theme.MindInkWhite

private val ContentHorizontalPadding = 20.dp

@Composable
fun GuestArticleDetailScreen(
    articleId: String,
    onBack: () -> Unit,
    onMyAccountClick: () -> Unit,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val article = state.article

    LaunchedEffect(articleId) {
        viewModel.setArticleId(articleId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        MindInkTopBar(
            screenTitle = "/ Lendo o Art.",
            user = null,
            onBack = onBack,
            showSearch = false,
            showUserMenu = false
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
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = ContentHorizontalPadding)
                        .padding(bottom = 28.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Escritor: ${formatAuthorName(article.authorName)}",
                        color = MindInkMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = article.title,
                        color = MindInkWhite,
                        fontSize = 26.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (article.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = article.subtitle,
                            color = MindInkMuted,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Markdown(
                        content = previewHalfContent(article.content),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MindInkBorder
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Está gostando do artigo?",
                        color = MindInkWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quer continuar lendo?",
                        color = MindInkWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Crie sua conta ou faça o login, para que seja liberado também a automação de leitura por voz.",
                        color = MindInkMuted,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    MyAccountButton(
                        onClick = onMyAccountClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun previewHalfContent(content: String): String {
    val trimmed = content.trim()
    if (trimmed.isEmpty()) return ""
    val half = (trimmed.length * 0.5f).toInt().coerceAtLeast(1)
    var cut = half
    while (cut < trimmed.length && !trimmed[cut].isWhitespace()) {
        cut++
    }
    val preview = trimmed.take(cut).trimEnd()
    return if (cut < trimmed.length) "$preview…" else preview
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
