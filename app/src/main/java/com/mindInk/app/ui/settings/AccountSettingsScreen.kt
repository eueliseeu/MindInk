package com.mindInk.app.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.components.MindInkTopBar
import com.mindInk.app.ui.components.UserAvatar
import com.mindInk.app.ui.theme.MindInkBlack
import com.mindInk.app.ui.theme.MindInkDim
import com.mindInk.app.ui.theme.MindInkInputSurface
import com.mindInk.app.ui.theme.MindInkMuted
import com.mindInk.app.ui.theme.MindInkRed
import com.mindInk.app.ui.theme.MindInkWhite

private val FieldShape = RoundedCornerShape(6.dp)

@Composable
fun AccountSettingsScreen(
    user: AuthUser,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onAccountDeleted: () -> Unit,
    onProfileUpdated: (String) -> Unit,
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(user.uid) {
        viewModel.load(user)
    }

    LaunchedEffect(state.accountDeleted) {
        if (state.accountDeleted) onAccountDeleted()
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { onProfileUpdated(state.name) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding()
    ) {
        MindInkTopBar(
            screenTitle = "Configurações",
            user = user,
            showSearch = false,
            showUserMenu = false,
            onSignOut = onSignOut
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MindInkWhite)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 32.dp)
                        .padding(top = 28.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(
                        photoUrl = state.photoUrl,
                        name = state.name,
                        size = 80.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = state.name.ifBlank { "Configurações da sua Conta" },
                        color = MindInkWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Todas as informações desse aplicativo se aplicam aos termos de usabilidade da LGPD",
                        color = MindInkWhite,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    SettingsFaqSection(
                        title = "Nome",
                        isOpenDefault = true
                    ) {
                        AccountField(
                            value = state.name,
                            onValueChange = viewModel::onNameChange,
                            label = "Nome",
                            placeholder = "Seu nome completo"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsFaqSection(
                        title = "Mídias Sociais",
                        isOpenDefault = false
                    ) {
                        Column {
                            AccountField(
                                value = state.github,
                                onValueChange = viewModel::onGithubChange,
                                label = "GitHub",
                                placeholder = "Seu usuário do github"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            AccountField(
                                value = state.linkedin,
                                onValueChange = viewModel::onLinkedinChange,
                                label = "LinkedIn",
                                placeholder = "Seu usuário linkedin"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsFaqSection(
                        title = "Zona de Perigo",
                        isOpenDefault = false
                    ) {
                        Column {
                            Text(
                                text = "A exclusão da conta é permanente e removerá todos os seus dados e seu acesso ao MindInk.",
                                color = MindInkMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = viewModel::showDeleteConfirmation,
                                enabled = !state.isSaving && !state.isDeleting,
                                shape = RoundedCornerShape(5.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9E3C3E),
                                    contentColor = MindInkWhite,
                                    disabledContainerColor = Color(0xFF9E3C3E).copy(alpha = 0.5f)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text("Excluir Conta", fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }

                    state.errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = message,
                            color = MindInkRed,
                            fontSize = 13.sp
                        )
                    }

                    state.successMessage?.let { message ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = message,
                            color = MindInkMuted,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::save,
                        enabled = !state.isSaving && !state.isDeleting,
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF303030),
                            contentColor = MindInkWhite,
                            disabledContainerColor = Color(0xFF303030).copy(alpha = 0.6f)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MindInkWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Atualizar Informações", fontSize = 12.sp, maxLines = 1)
                        }
                    }

                    if (state.isDeleting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MindInkWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Versão 1.0.0",
                        color = MindInkMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirmation,
            title = { Text("Excluir conta?") },
            text = { Text("Essa ação é permanente e removerá seu acesso ao MindInk.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmDeleteAccount,
                    colors = ButtonDefaults.textButtonColors(contentColor = MindInkRed)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirmation) {
                    Text("Cancelar")
                }
            },
            containerColor = MindInkBlack,
            titleContentColor = MindInkWhite,
            textContentColor = MindInkMuted
        )
    }
}

@Composable
private fun SettingsFaqSection(
    title: String,
    isOpenDefault: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(isOpenDefault) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MindInkDim.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = MindInkWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MindInkWhite
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun AccountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    readOnly: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            singleLine = true,
            shape = FieldShape,
            placeholder = {
                if (placeholder.isNotBlank()) {
                    Text(text = placeholder, color = MindInkMuted, fontSize = 14.sp)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MindInkWhite,
                unfocusedTextColor = MindInkWhite,
                focusedContainerColor = MindInkInputSurface,
                unfocusedContainerColor = MindInkInputSurface,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MindInkRed
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
    }
}