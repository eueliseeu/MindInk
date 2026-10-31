package com.mindInk.app.ui.articles.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkWhite

@Composable
fun ContinueReadingSection(
    articles: List<ArticleCardUi>,
    currentUid: String?,
    onArticleClick: (ArticleCardUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Continue Lendo",
            color = MindInkWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Artigos que você abriu e ainda não terminou de ler.",
            color = MindInkMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (articles.isEmpty()) {
            Text(
                text = "Nenhum artigo pendente por aqui.",
                color = MindInkMuted,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        } else {
            articles.forEach { article ->
                ExploreArticleRow(
                    article = article,
                    currentUid = currentUid,
                    onClick = { onArticleClick(article) }
                )
            }
        }
    }
}