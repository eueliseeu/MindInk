package com.mindInk.app.ui.articles.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindInk.app.ui.articles.model.MuralItemUi
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkWhite

@Composable
fun MuralSection(
    items: List<MuralItemUi>,
    onNewArticleClick: (MuralItemUi.NewArticleFromFavorite) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Mural",
            color = MindInkWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Text(
                text = "Nenhuma notificação ainda.",
                color = MindInkMuted,
                fontSize = 14.sp
            )
        } else {
            items.forEach { item ->
                val clickable = item is MuralItemUi.NewArticleFromFavorite
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (clickable) {
                                Modifier.clickable {
                                    onNewArticleClick(item as MuralItemUi.NewArticleFromFavorite)
                                }
                            } else {
                                Modifier
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        photoUrl = item.actorPhotoUrl,
                        name = item.actorName,
                        size = 40.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (item) {
                                is MuralItemUi.StarReceived ->
                                    "${item.actorName} deixou uma estrela no seu artigo"
                                is MuralItemUi.NewArticleFromFavorite ->
                                    "${item.actorName} publicou um novo artigo"
                            },
                            color = MindInkWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.articleTitle,
                            color = MindInkMuted,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}