package com.mindInk.app.ui.articles.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.theme.MindInkBackground
import com.mindInk.app.ui.theme.MindInkDim
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkRed
import com.mindInk.app.ui.theme.MindInkSurface
import com.mindInk.app.ui.theme.MindInkWhite

@Composable
fun CreateEditArticleScreen(
    user: AuthUser,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSignOut: () -> Unit = {},
    viewModel: CreateEditArticleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(user.uid) {
        viewModel.bindAuthor(user)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindInkBackground)
            .navigationBarsPadding()
    ) {
        MindInkTopBar(
            screenTitle = if (state.isEditMode) "/ Editar Artigo" else "/ Novo Artigo",
            user = user,
            onBack = null, // Removida a seta padrão da TopBar
            showSearch = false,
            showUserMenu = true,
            onSignOut = onSignOut
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Retornar",
                color = MindInkWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onBack)
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Escritor: ${state.authorLabel}",
                color = MindInkMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            BasicTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                textStyle = TextStyle(
                    color = MindInkWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                ),
                cursorBrush = SolidColor(MindInkRed),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Box {
                        if (state.title.isEmpty()) {
                            Text(
                                text = "Título do artigo",
                                color = MindInkDim,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        inner()
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            BasicTextField(
                value = state.subtitle,
                onValueChange = viewModel::onSubtitleChange,
                textStyle = TextStyle(
                    color = MindInkMuted,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(MindInkRed),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Box {
                        if (state.subtitle.isEmpty()) {
                            Text(
                                text = "Subtítulo",
                                color = MindInkDim,
                                fontSize = 15.sp
                            )
                        }
                        inner()
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.isPreview) "Preview" else "Markdown",
                    color = MindInkMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = viewModel::togglePreview) {
                    Icon(
                        imageVector = if (state.isPreview) {
                            Icons.Rounded.VisibilityOff
                        } else {
                            Icons.Rounded.Visibility
                        },
                        contentDescription = "Alternar preview",
                        tint = MindInkWhite
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
                    .background(MindInkSurface, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (state.isPreview) {
                    if (state.content.isBlank()) {
                        Text(
                            text = "Nada para pré-visualizar.",
                            color = MindInkDim,
                            fontSize = 14.sp
                        )
                    } else {
                        Markdown(content = state.content)
                    }
                } else {
                    BasicTextField(
                        value = state.content,
                        onValueChange = viewModel::onContentChange,
                        textStyle = TextStyle(
                            color = MindInkWhite,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ),
                        cursorBrush = SolidColor(MindInkRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 288.dp),
                        decorationBox = { inner ->
                            Box {
                                if (state.content.isEmpty()) {
                                    Text(
                                        text = "Escreva em Markdown…",
                                        color = MindInkDim,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MindInkWhite,
                    contentColor = MindInkBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (state.isEditMode) "Salvar alterações" else "Publicar artigo",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
