package com.mindInk.app.ui.articles.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindInk.app.ui.articles.formatCount
import com.mindInk.app.ui.articles.model.ArticleCardUi
import com.mindInk.app.ui.articles.postedAgoText
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkStar
import com.mindInk.app.ui.theme.MindInkSurface
import com.mindInk.app.ui.theme.MindInkWhite

private val RowShape = RoundedCornerShape(16.dp)

@Composable
fun ExploreArticleRow(
    article: ArticleCardUi,
    currentUid: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RowShape)
            .border(1.dp, MindInkBorder, RowShape)
            .background(MindInkSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.category.uppercase(),
                    color = MindInkMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = postedAgoText(article.createdAt),
                    color = MindInkMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.title,
                color = MindInkWhite,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (article.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.summary,
                    color = MindInkMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                UserAvatar(
                    photoUrl = article.authorPhotoUrl,
                    name = article.authorName,
                    size = 24.dp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = article.authorName,
                    color = MindInkMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = if (currentUid != null && currentUid in article.starredBy) {
                        MindInkStar
                    } else {
                        MindInkWhite
                    },
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = formatCount(article.starCount),
                    color = MindInkWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
