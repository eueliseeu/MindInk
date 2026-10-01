package com.mindInk.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mindInk.app.R
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkSurfaceVariant
import com.mindInk.app.ui.theme.MindInkWhite

private val LogoSize = 44.dp
private val SearchButtonSize = 44.dp
private val AvatarSize = 48.dp

// Espaço entre a status bar e o conteúdo da barra
private val TopBarTopPadding = 28.dp

// Distância entre o avatar e o menu aberto
private val MenuGap = 6.dp

@Composable
fun MindInkTopBar(
    screenTitle: String,
    user: AuthUser,
    onSearchClick: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = TopBarTopPadding, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_mindink_logo),
            contentDescription = "MindInk",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(LogoSize)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = stringResource(R.string.topbar_title_format, screenTitle),
            color = MindInkWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.size(SearchButtonSize),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black,
                contentColor = MindInkWhite
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.topbar_search),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        UserMenu(
            user = user,
            onSignOut = onSignOut
        )
    }
}

@Composable
private fun UserMenu(
    user: AuthUser,
    onSignOut: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val menuOffsetY = with(LocalDensity.current) { (AvatarSize + MenuGap).roundToPx() }

    Box {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    onClickLabel = stringResource(R.string.topbar_open_menu),
                    role = Role.Button
                ) { expanded = true }
        ) {
            UserAvatar(
                photoUrl = user.photoUrl,
                name = user.displayName ?: user.email,
                size = AvatarSize
            )
        }

        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(x = 0, y = menuOffsetY),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MindInkSurfaceVariant,
                    border = BorderStroke(1.dp, MindInkBorder)
                ) {
                    Text(
                        text = stringResource(R.string.topbar_sign_out),
                        color = MindInkWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable {
                                expanded = false
                                onSignOut()
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
