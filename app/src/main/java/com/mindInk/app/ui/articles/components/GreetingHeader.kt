package com.mindInk.app.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkWhite

@Composable
fun GreetingHeader(
    firstName: String,
    modifier: Modifier = Modifier,
    onSettingsClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Bem-vindo(a) de volta",
            color = MindInkMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Olá, $firstName",
            color = MindInkWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        if (onSettingsClick != null) {
            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onSettingsClick() }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Minhas Configurações",
                    color = MindInkMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MindInkMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}