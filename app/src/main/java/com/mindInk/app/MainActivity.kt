package com.mindInk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.ui.articles.ArticlesScreen
import com.mindInk.app.ui.articles.detail.ArticleDetailScreen
import com.mindInk.app.ui.articles.editor.CreateEditArticleScreen
import com.mindInk.app.ui.auth.AuthViewModel
import com.mindInk.app.ui.auth.LinkAccountDialog
import com.mindInk.app.ui.auth.LoginScreen
import com.mindInk.app.ui.auth.SessionState
import com.mindInk.app.ui.auth.toMessageRes
import com.mindInk.app.ui.components.ToastHost
import com.mindInk.app.ui.components.ToastMessage
import com.mindInk.app.ui.profile.AuthorProfileScreen
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
                val resources by rememberUpdatedState(LocalResources.current)
                val viewModel: AuthViewModel = hiltViewModel()
                val session by viewModel.sessionState.collectAsState()
                val uiState by viewModel.uiState.collectAsState()
                var showSplash by rememberSaveable { mutableStateOf(true) }
                var showCreateArticle by rememberSaveable { mutableStateOf(false) }
                var selectedArticleId by rememberSaveable { mutableStateOf<String?>(null) }
                var selectedAuthorUid by rememberSaveable { mutableStateOf<String?>(null) }
                var toast by remember { mutableStateOf<ToastMessage?>(null) }

                LaunchedEffect(viewModel) {
                    viewModel.errorEvents.collect { reason ->
                        toast = ToastMessage(resources.getString(reason.toMessageRes()))
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (showSplash || session is SessionState.Loading) {
                        SplashScreen(
                            onFinished = { showSplash = false }
                        )
                    } else {
                        val current = session
                        when {
                            current is SessionState.Authenticated && !uiState.isLoading -> {
                                when {
                                    selectedArticleId != null -> {
                                        ArticleDetailScreen(
                                            articleId = selectedArticleId!!,
                                            user = current.user,
                                            onBack = { selectedArticleId = null },
                                            onSignOut = viewModel::onSignOutClick
                                        )
                                    }

                                    selectedAuthorUid != null -> {
                                        AuthorProfileScreen(
                                            authorUid = selectedAuthorUid!!,
                                            user = current.user,
                                            onBack = { selectedAuthorUid = null },
                                            onArticleClick = { card ->
                                                selectedAuthorUid = null
                                                selectedArticleId = card.id
                                            },
                                            onSignOut = viewModel::onSignOutClick
                                        )
                                    }

                                    showCreateArticle -> {
                                        CreateEditArticleScreen(
                                            user = current.user,
                                            onBack = { showCreateArticle = false },
                                            onSaved = {
                                                showCreateArticle = false
                                                toast = ToastMessage("Artigo publicado.")
                                            },
                                            onSignOut = viewModel::onSignOutClick
                                        )
                                    }

                                    else -> {
                                        ArticlesScreen(
                                            user = current.user,
                                            onSignOut = viewModel::onSignOutClick,
                                            onAddArticleClick = { showCreateArticle = true },
                                            onArticleClick = { card ->
                                                selectedArticleId = card.id
                                            },
                                            onAuthorClick = { author ->
                                                selectedAuthorUid = author.uid
                                            }
                                        )
                                    }
                                }
                            }

                            else -> {
                                showCreateArticle = false
                                selectedArticleId = null
                                selectedAuthorUid = null
                                LoginScreen(
                                    loadingProvider = uiState.loadingProvider,
                                    onGoogleClick = { viewModel.onGoogleClick(this@MainActivity) },
                                    onGitHubClick = { viewModel.onGitHubClick(this@MainActivity) }
                                )
                            }
                        }
                    }

                    ToastHost(
                        message = toast,
                        onDismiss = { toast = null }
                    )

                    uiState.linkRequest?.let { request ->
                        LinkAccountDialog(
                            request = request,
                            onConfirm = { viewModel.onConfirmLink(this@MainActivity) },
                            onDismiss = viewModel::onDismissLink
                        )
                    }
                }
            }
        }
    }
}
