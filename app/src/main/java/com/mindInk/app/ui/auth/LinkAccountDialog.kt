package com.mindInk.app.ui.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindInk.app.R
import com.mindInk.app.domain.model.AuthProvider
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkSurfaceVariant
import com.mindInk.app.ui.theme.MindInkWhite

@Composable
fun LinkAccountDialog(
    request: LinkRequest,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val existingName = stringResource(request.existingProvider.toNameRes())
    val pendingName = stringResource(request.pendingProvider.toNameRes())

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MindInkSurfaceVariant,
        titleContentColor = MindInkWhite,
        textContentColor = MindInkMuted,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(R.string.link_dialog_title),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.link_dialog_message, existingName, pendingName)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.link_dialog_confirm),
                    color = MindInkWhite,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.link_dialog_cancel),
                    color = MindInkMuted
                )
            }
        }
    )
}

@StringRes
private fun AuthProvider.toNameRes(): Int = when (this) {
    AuthProvider.GOOGLE -> R.string.provider_google
    AuthProvider.GITHUB -> R.string.provider_github
    AuthProvider.UNKNOWN -> R.string.provider_unknown
}
