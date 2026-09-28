package com.mindInk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mindInk.app.ui.auth.LoginScreen
import com.mindInk.app.ui.splash.SplashScreen
import com.mindInk.app.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onFinished = { showSplash = false }
                    )
                } else {
                    LoginScreen(
                        onGoogleClick = {
                            // Auth no próximo passo
                        },
                        onGitHubClick = {
                            // Auth no próximo passo
                        }
                    )
                }
            }
        }
    }
}
