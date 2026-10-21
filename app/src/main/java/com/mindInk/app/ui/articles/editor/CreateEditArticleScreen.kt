package com.mindInk.app.ui.articles.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.mindInk.app.core.MAX_ARTICLE_WORDS
import com.mindInk.app.core.countWords
import com.mindInk.app.domain.model.ArticleCategories
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.theme.MindInkBlack
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkDim
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkRed
import com.mindInk.app.ui.theme.MindInkWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditArticleScreen(
    user: AuthUser,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSignOut: () -> Unit = {},
    onProfileClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    viewModel: CreateEditArticleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var categoryExpanded by remember { mutableStateOf(false) }
    val wordCount = remember(state.content) { countWords(state.content) }

    LaunchedEffect(user.uid) {
        if (!state.isEditMode) {
            viewModel.resetForNewArticle(user)
        } else {
            viewModel.bindAuthor(user)
        }
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
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding()
    ) {
        MindInkTopBar(
            screenTitle = if (state.isEditMode) "/ Editar Artigo" else "/ Novo Artigo",
            user = user,
            onBack = onBack,
            showSearch = false,
            showUserMenu = true,
            onSignOut = onSignOut,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MindInkBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.category.ifBlank { "Selecione a categoria" },
                            color = if (state.category.isBlank()) MindInkDim else MindInkWhite,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            tint = MindInkWhite
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    containerColor = MindInkBlack
                ) {
                    ArticleCategories.ALL.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = category,
                                    color = MindInkWhite,
                                    fontSize = 15.sp
                                )
                            },
                            onClick = {
                                viewModel.onCategorySelect(category)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MindInkBlack,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BasicTextField(
                        value = state.title,
                        onValueChange = viewModel::onTitleChange,
                        textStyle = TextStyle(
                            color = MindInkWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 30.sp
                        ),
                        cursorBrush = SolidColor(MindInkRed),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box {
                                if (state.title.isEmpty()) {
                                    Text(
                                        text = "Título do artigo",
                                        color = MindInkDim,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                inner()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MindInkBorder.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(10.dp))

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
                                        text = "Subtítulo (opcional)",
                                        color = MindInkDim,
                                        fontSize = 15.sp
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MindInkBlack,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 4.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.isPreview) "Preview" else "Editor",
                            color = MindInkMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
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

                    if (!state.isPreview) {
                        MarkdownToolbar(
                            onBold = viewModel::applyBold,
                            onItalic = viewModel::applyItalic,
                            onHeading = viewModel::applyHeading,
                            onBullet = viewModel::applyBullet,
                            onLink = viewModel::applyLink,
                            onCode = viewModel::applyCode
                        )
                        HorizontalDivider(color = MindInkBorder.copy(alpha = 0.5f))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 280.dp)
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
                                    .heightIn(min = 248.dp),
                                decorationBox = { inner ->
                                    Box {
                                        if (state.content.isEmpty()) {
                                            Text(
                                                text = "Escreva o artigo em Markdown…\nUse a barra acima para formatação.",
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
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$wordCount / $MAX_ARTICLE_WORDS palavras",
                color = if (wordCount >= MAX_ARTICLE_WORDS) MindInkRed else MindInkDim,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            state.errorMessage?.let { errorMessage ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = viewModel::save,
            enabled = !state.isSaving,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MindInkWhite,
                contentColor = MindInkBlack,
                disabledContainerColor = MindInkWhite.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(52.dp)
        ) {
            Text(
                text = when {
                    state.isSaving -> "Publicando…"
                    state.isEditMode -> "Salvar alterações"
                    else -> "Publicar artigo"
                },
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun MarkdownToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onHeading: () -> Unit,
    onBullet: () -> Unit,
    onLink: () -> Unit,
    onCode: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarIcon(Icons.Rounded.FormatBold, "Negrito", onBold)
        ToolbarIcon(Icons.Rounded.FormatItalic, "Itálico", onItalic)
        ToolbarIcon(Icons.Rounded.Title, "Título", onHeading)
        ToolbarIcon(Icons.Rounded.FormatListBulleted, "Lista", onBullet)
        ToolbarIcon(Icons.Rounded.Link, "Link", onLink)
        ToolbarIcon(Icons.Rounded.Code, "Código", onCode)
    }
}

@Composable
private fun ToolbarIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MindInkWhite,
            containerColor = Color.Transparent
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}
