package com.mindInk.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindInk.app.domain.model.AccountSettings
import com.mindInk.app.domain.model.AuthUser
import com.mindInk.app.domain.repository.DeleteAccountResult
import com.mindInk.app.domain.repository.LoadAccountSettingsResult
import com.mindInk.app.domain.repository.SaveAccountSettingsResult
import com.mindInk.app.domain.repository.UpdateAuthorArticlesResult
import com.mindInk.app.domain.usecase.auth.DeleteAccountUseCase
import com.mindInk.app.domain.usecase.auth.UpdateDisplayNameUseCase
import com.mindInk.app.domain.usecase.article.UpdateAuthorArticlesUseCase
import com.mindInk.app.domain.usecase.settings.LoadAccountSettingsUseCase
import com.mindInk.app.domain.usecase.settings.SaveAccountSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val loadSettings: LoadAccountSettingsUseCase,
    private val saveSettings: SaveAccountSettingsUseCase,
    private val updateAuthorArticles: UpdateAuthorArticlesUseCase,
    private val updateDisplayName: UpdateDisplayNameUseCase,
    private val deleteAccount: DeleteAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountSettingsUiState())
    val uiState: StateFlow<AccountSettingsUiState> = _uiState.asStateFlow()

    fun load(user: AuthUser) {
        if (_uiState.value.uid == user.uid && !_uiState.value.isLoading) return

        _uiState.value = AccountSettingsUiState(
            uid = user.uid,
            name = user.displayName.orEmpty().ifBlank { user.email.orEmpty().substringBefore("@") },
            email = user.email.orEmpty(),
            isLoading = true
        )

        viewModelScope.launch {
            when (val result = loadSettings(user.uid)) {
                is LoadAccountSettingsResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            name = result.settings.name.ifBlank { state.name },
                            github = result.settings.github,
                            linkedin = result.settings.linkedin
                        )
                    }
                }
                is LoadAccountSettingsResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update {
        it.copy(name = value, errorMessage = null, successMessage = null)
    }

    fun onGithubChange(value: String) = _uiState.update {
        it.copy(github = value, errorMessage = null, successMessage = null)
    }

    fun onLinkedinChange(value: String) = _uiState.update {
        it.copy(linkedin = value, errorMessage = null, successMessage = null)
    }

    fun save() {
        val current = _uiState.value
        val name = current.name.trim()

        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe seu nome.") }
            return
        }
        if (current.isSaving) return

        val settings = AccountSettings(
            name = name,
            github = current.github.trim(),
            linkedin = current.linkedin.trim()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            when (val result = saveSettings(current.uid, settings)) {
                SaveAccountSettingsResult.Success -> {
                    when (val result = updateAuthorArticles(current.uid, name)) {
                        UpdateAuthorArticlesResult.Success -> {
                            try {
                                updateDisplayName(name)
                                _uiState.update {
                                    it.copy(
                                        isSaving = false,
                                        name = name,
                                        successMessage = "Informações atualizadas."
                                    )
                                }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                _uiState.update {
                                    it.copy(
                                        isSaving = false,
                                        name = name,
                                        errorMessage = "Artigos atualizados, mas não foi possível atualizar o nome da conta."
                                    )
                                }
                            }
                        }
                        is UpdateAuthorArticlesResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    name = name,
                                    errorMessage = "Configurações salvas, mas os artigos não foram sincronizados. Tente salvar novamente."
                                )
                            }
                        }
                    }
                }
                is SaveAccountSettingsResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun showDeleteConfirmation() = _uiState.update {
        it.copy(showDeleteConfirmation = true, errorMessage = null)
    }

    fun dismissDeleteConfirmation() = _uiState.update {
        it.copy(showDeleteConfirmation = false)
    }

    fun confirmDeleteAccount() {
        if (_uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isDeleting = true, errorMessage = null, showDeleteConfirmation = false)
            }
            when (val result = deleteAccount()) {
                DeleteAccountResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false, accountDeleted = true) }
                }
                is DeleteAccountResult.Error -> {
                    _uiState.update { it.copy(isDeleting = false, errorMessage = result.message) }
                }
            }
        }
    }
}
