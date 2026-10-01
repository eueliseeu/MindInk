package com.mindInk.app.ui.articles.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindInk.app.R
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkWhite

private val HorizontalPadding = 20.dp

// Igual à margem lateral: o próximo card começa exatamente fora da tela, sem faixa aparecendo
private val CardSpacing = HorizontalPadding

// Largura do card em relação à área entre as margens: 1f = card ocupa a tela inteira.
// Abaixo de 1f o próximo card passa a aparecer cortado na lateral (ex.: 0.8f)
private const val CardWidthFraction = 1f

@Composable
fun LatestArticlesSection(
    articles: List<ArticleCardUi>,
    onArticleClick: (ArticleCardUi) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = HorizontalPadding, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.articles_latest_title),
                color = MindInkWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.articles_add),
                    tint = MindInkMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val listState = rememberLazyListState()
        val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = HorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(CardSpacing)
        ) {
            items(
                items = articles,
                key = { it.id }
            ) { article ->
                ArticleCard(
                    article = article,
                    onClick = { onArticleClick(article) },
                    modifier = Modifier.fillParentMaxWidth(CardWidthFraction)
                )
            }
        }
    }
}
