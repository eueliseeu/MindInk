package com.mindInk.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkSurfaceVariant
import com.mindInk.app.ui.theme.MindInkWhite
import kotlinx.coroutines.delay

private const val TOAST_DURATION_MS = 3500L
private const val FADE_IN_MS = 250
private const val FADE_OUT_MS = 300

data class ToastMessage(
    val text: String,
    val id: Long = System.nanoTime()
)

@Composable
fun ToastHost(
    message: ToastMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    var lastText by remember { mutableStateOf("") }

    LaunchedEffect(message) {
        if (message != null) {
            lastText = message.text
            delay(TOAST_DURATION_MS)
            currentOnDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn(tween(FADE_IN_MS)),
            exit = fadeOut(tween(FADE_OUT_MS))
        ) {
            val shape = RoundedCornerShape(24.dp)
            Text(
                text = message?.text ?: lastText,
                color = MindInkWhite,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .semantics { liveRegion = LiveRegionMode.Polite }
                    .background(MindInkSurfaceVariant, shape)
                    .border(1.dp, MindInkBorder, shape)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}
