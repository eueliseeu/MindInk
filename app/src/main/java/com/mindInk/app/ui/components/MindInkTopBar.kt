
package com.mindInk.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mindInk.app.R
import com.mindInk.app.core.formatAuthorName
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkSurfaceVariant
import com.mindInk.app.ui.theme.MindInkWhite

private val LogoSize = 38.dp
private val SearchButtonSize = 40.dp
private val AvatarSize = 42.dp
private val TopBarTopPadding = 24.dp
private val TopBarBottomPadding = 12.dp
private val MenuGap = 8.dp

@Composable
fun MindInkTopBar(
    screenTitle: String,
    user: AuthUser? = null,
    onSearchClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    showSearch: Boolean = true,
    showUserMenu: Boolean = true,
    trailingText: String? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val canShowUser = user != null && showUserMenu
    val displayName = formatAuthorName(user?.displayName ?: user?.email)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = TopBarTopPadding,
                bottom = TopBarBottomPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(LogoSize)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MindInkWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
        }

        Image(
            painter = painterResource(id = R.drawable.ic_mindink_logo),
            contentDescription = "MindInk",
            modifier = Modifier.size(LogoSize)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = screenTitle,
            color = MindInkWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (!trailingText.isNullOrBlank()) {
            Text(
                text = trailingText,
                color = MindInkMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        if (canShowUser && user != null) {
            Box {
                UserAvatar(
                    photoUrl = user.photoUrl,
                    name = user.displayName,
                    size = AvatarSize,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClick = { menuExpanded = true }
                        )
                )

                if (menuExpanded) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(
                            x = 0,
                            y = with(density) {
                                (AvatarSize + MenuGap).roundToPx()
                            }
                        ),
                        onDismissRequest = { menuExpanded = false },
                        properties = PopupProperties(focusable = true)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MindInkSurfaceVariant,
                            border = BorderStroke(1.dp, MindInkBorder),
                            shadowElevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier.width(200.dp)
                            ) {
                                Text(
                                    text = displayName,
                                    color = MindInkWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MindInkBorder)
                                )

                                if (onProfileClick != null) {
                                    Text(
                                        text = "Perfil",
                                        color = MindInkWhite,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                menuExpanded = false
                                                onProfileClick()
                                            }
                                            .padding(
                                                horizontal = 16.dp,
                                                vertical = 12.dp
                                            )
                                    )
                                }

                                if (onSettingsClick != null) {
                                    Text(
                                        text = "Configurações",
                                        color = MindInkWhite,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                menuExpanded = false
                                                onSettingsClick()
                                            }
                                            .padding(
                                                horizontal = 16.dp,
                                                vertical = 12.dp
                                            )
                                    )
                                }

                                Text(
                                    text = "Sair",
                                    color = MindInkWhite,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            menuExpanded = false
                                            onSignOut()
                                        }
                                        .padding(
                                            horizontal = 16.dp,
                                            vertical = 12.dp
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
