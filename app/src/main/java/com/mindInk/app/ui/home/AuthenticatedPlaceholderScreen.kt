package com.mindInk.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindInk.app.R
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.theme.MindInkBlack
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkRed
import com.mindInk.app.ui.theme.MindInkWhite

@Composable
fun AuthenticatedPlaceholderScreen(
    user: AuthUser,
    onSignOut: () -> Unit
) {
    val title = user.displayName ?: user.email ?: user.uid
    // E-mail só vira subtítulo quando o título já é o nome
    val subtitle = if (user.displayName != null) user.email else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindInkBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.home_placeholder_signed_in_as),
            color = MindInkMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        UserAvatar(
            photoUrl = user.photoUrl,
            name = title
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            color = MindInkWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = MindInkMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = user.provider.name,
            color = MindInkMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSignOut,
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MindInkRed,
                contentColor = MindInkWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = stringResource(R.string.home_placeholder_sign_out),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
