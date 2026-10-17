package com.mindInk.app.ui.articles.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindInk.app.ui.articles.model.AuthorUi
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.theme.MindInkWhite

private val HorizontalPadding = 20.dp
private val AvatarSize = 64.dp

@Composable
fun AuthorsSection(
    authors: List<AuthorUi>,
    modifier: Modifier = Modifier
) {
    if (authors.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Autores",
            color = MindInkWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = HorizontalPadding)
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = HorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                items = authors,
                key = { it.uid }
            ) { author ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    UserAvatar(
                        photoUrl = author.photoUrl,
                        name = author.name,
                        size = AvatarSize
                    )
                }
            }
        }
    }
}
