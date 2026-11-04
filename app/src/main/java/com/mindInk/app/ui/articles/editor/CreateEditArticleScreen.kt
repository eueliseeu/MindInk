package com.mindInk.app.ui.articles.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.mindInk.app.core.MAX_ARTICLE_WORDS
import com.mindInk.app.core.countWords
import com.mindInk.app.core.normalizeMarkdown
import com.mindInk.app.domain.model.ArticleCategories
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.theme.MindInkBackground
import com.mindInk.app.ui.theme.MindInkBlack
import com.mindInk.app.ui.theme.MindInkBorder
import com.mindInk.app.ui.theme.MindInkDim
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkRed
import com.mindInk.app.ui.theme.MindInkWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val focusManager = LocalFocusManager.current
    var categoryExpanded by remember { mutableStateOf(false) }
    var contentFocused by remember { mutableStateOf(false) }

    val imeVisible = WindowInsets.isImeVisible
    val showToolbar = imeVisible && contentFocused && !state.isPreview
    val wordCount = remember(state.content.text) { countWords(state.content.text) }
    val previewContent = remember(state.content.text, state.isPreview) {
        if (state.isPreview) normalizeMarkdown(state.content.text) else ""
    }

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
            screenTitle = if (state.isEditMode) "Editar Artigo" else "Novo Artigo",
            user = user,
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
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
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
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
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
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EditorTabs(
                            isPreview = state.isPreview,
                            onSelect = { preview ->
                                if (preview) focusManager.clearFocus()
                                viewModel.onPreviewChange(preview)
                            }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "$wordCount / $MAX_ARTICLE_WORDS",
                            color = if (wordCount >= MAX_ARTICLE_WORDS) MindInkRed else MindInkDim,
                            fontSize = 12.sp
                        )
                    }

                    HorizontalDivider(color = MindInkBorder.copy(alpha = 0.5f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 320.dp)
                            .padding(16.dp)
                    ) {
                        if (state.isPreview) {
                            ArticlePreview(
                                title = state.title,
                                subtitle = state.subtitle,
                                content = previewContent
                            )
                        } else {
                            BasicTextField(
                                value = state.content,
                                onValueChange = viewModel::onContentChange,
                                textStyle = TextStyle(
                                    color = MindInkWhite,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                ),
                                cursorBrush = SolidColor(MindInkRed),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 288.dp)
                                    .onFocusChanged { contentFocused = it.isFocused },
                                decorationBox = { inner ->
                                    Box {
                                        if (state.content.text.isEmpty()) {
                                            Text(
                                                text = "Escreva o artigo em Markdown…\nSelecione um trecho e use a barra de formatação.",
                                                color = MindInkDim,
                                                fontSize = 16.sp,
                                                lineHeight = 24.sp
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

        AnimatedVisibility(visible = showToolbar) {
            MarkdownToolbar(
                onBold = viewModel::applyBold,
                onItalic = viewModel::applyItalic,
                onHeading = viewModel::applyHeading,
                onBullet = viewModel::applyBullet,
                onNumbered = viewModel::applyNumbered,
                onQuote = viewModel::applyQuote,
                onLink = viewModel::applyLink,
                onCode = viewModel::applyCode
            )
        }

        AnimatedVisibility(visible = !imeVisible) {
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
}

@Composable
private fun ArticlePreview(
    title: String,
    subtitle: String,
    content: String
) {
    if (content.isBlank()) {
        Text(
            text = "Nada para pré-visualizar.",
            color = MindInkDim,
            fontSize = 14.sp
        )
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                color = MindInkWhite,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = MindInkMuted,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
        if (title.isNotBlank() || subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MindInkBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(14.dp))
        }
        Markdown(content = content)
    }
}

@Composable
private fun EditorTabs(
    isPreview: Boolean,
    onSelect: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MindInkBackground)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        EditorTab(label = "Escrever", selected = !isPreview) { onSelect(false) }
        EditorTab(label = "Preview", selected = isPreview) { onSelect(true) }
    }
}

@Composable
private fun EditorTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MindInkBorder else Color.Transparent)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) MindInkWhite else MindInkMuted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun MarkdownToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onHeading: () -> Unit,
    onBullet: () -> Unit,
    onNumbered: () -> Unit,
    onQuote: () -> Unit,
    onLink: () -> Unit,
    onCode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MindInkBlack)
    ) {
        HorizontalDivider(color = MindInkBorder.copy(alpha = 0.6f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarIcon(Icons.Rounded.FormatBold, "Negrito", onBold)
            ToolbarIcon(Icons.Rounded.FormatItalic, "Itálico", onItalic)
            ToolbarIcon(Icons.Rounded.Title, "Título", onHeading)
            ToolbarIcon(Icons.Rounded.FormatListBulleted, "Lista", onBullet)
            ToolbarIcon(Icons.Rounded.FormatListNumbered, "Lista numerada", onNumbered)
            ToolbarIcon(Icons.Rounded.FormatQuote, "Citação", onQuote)
            ToolbarIcon(Icons.Rounded.Link, "Link", onLink)
            ToolbarIcon(Icons.Rounded.Code, "Código", onCode)
        }
    }
}

@Composable
private fun RowScope.ToolbarIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(46.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MindInkWhite,
            modifier = Modifier.size(20.dp)
        )
    }
}