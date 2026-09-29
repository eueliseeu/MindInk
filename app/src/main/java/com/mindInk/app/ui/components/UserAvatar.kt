package com.mindInk.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkSurfaceVariant
import com.mindInk.app.ui.theme.MindInkWhite

@Composable
fun UserAvatar(
    photoUrl: String?,
    name: String?,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val initial = name?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MindInkSurfaceVariant)
            .border(1.dp, MindInkBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = MindInkWhite,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.SemiBold
        )

        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
