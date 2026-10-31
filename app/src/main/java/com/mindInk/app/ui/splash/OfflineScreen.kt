
package com.mindInk.app.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindInk.app.R
import com.mindInk.app.ui.theme.MindInkBlack
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkWhite

private val LogoSize = 38.dp
private val TopBarTopPadding = 24.dp
private val TopBarBottomPadding = 12.dp

@Composable
fun OfflineScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindInkBlack)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = TopBarTopPadding,
                    bottom = TopBarBottomPadding
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mindink_logo),
                    contentDescription = null,
                    modifier = Modifier.size(LogoSize)
                )

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = " / MindInk.",
                    color = MindInkWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Offline",
                color = MindInkMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.error_connect),
                contentDescription = "Sem conexão",
                modifier = Modifier.size(260.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Ops... Error43",
                color = MindInkWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Parece que você está sem conexão com a internet\n" +
                        "MindInk precisa dessa conexão.\n" +
                        "Tente novamente mais tarde",
                color = MindInkMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
