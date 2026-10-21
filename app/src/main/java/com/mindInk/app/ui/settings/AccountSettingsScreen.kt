package com.mindInk.app.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.ui.components.MindInkTopBar
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
            screenTitle = "/ Configurações",
            user = user,
            onBack = onBack,
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
                        .padding(top = 44.dp, bottom = 24.dp)
                ) {
                    Text(
                        text = "Configurações da sua Conta",
                        color = MindInkWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Todas as informações desse aplicativo se aplicam aos termos de usabilidade da LGPD",
                        color = MindInkWhite,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    AccountField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Nome"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AccountField(
                        value = state.email,
                        onValueChange = {},
                        label = "E-mail",
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    Text(
                        text = "Mídias Sociais",
                        color = MindInkWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AccountField(
                        value = state.github,
                        onValueChange = viewModel::onGithubChange,
                        label = "GitHub"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AccountField(
                        value = state.linkedin,
                        onValueChange = viewModel::onLinkedinChange,
                        label = "LinkedIn"
                    )

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

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("Excluir Conta", fontSize = 12.sp, maxLines = 1)
                        }

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
                                .weight(1f)
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
                    }

                    if (state.isDeleting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MindInkWhite
                        )
                    }
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
private fun AccountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = MindInkWhite,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            singleLine = true,
            shape = FieldShape,
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
