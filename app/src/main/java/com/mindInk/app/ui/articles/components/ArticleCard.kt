package com.mindInk.app.ui.articles.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindInk.app.R
import com.mindInk.app.ui.articles.formatCount
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.postedAgoText
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkWhite

private val CardShape = RoundedCornerShape(28.dp)
private val AuthorAvatarSize = 32.dp

@Composable
fun ArticleCard(
    article: ArticleCardUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(Color.Black)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryTag(text = article.category)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = postedAgoText(article.createdAt),
                color = MindInkWhite,
                fontSize = 11.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = article.title,
            color = MindInkWhite,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = article.summary,
            color = MindInkMuted,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                photoUrl = article.authorPhotoUrl,
                name = article.authorName,
                size = AuthorAvatarSize
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = article.authorName,
                color = MindInkWhite,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = stringResource(R.string.article_stars),
                tint = MindInkWhite,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = formatCount(article.starCount),
                color = MindInkWhite,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CategoryTag(text: String) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier
            .background(MindInkWhite, CircleShape)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}
