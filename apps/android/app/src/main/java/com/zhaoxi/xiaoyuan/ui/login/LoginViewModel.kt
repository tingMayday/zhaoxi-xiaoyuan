package com.zhaoxi.xiaoyuan.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.auth.AuthRepository
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val mode: Mode = Mode.Login,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
) {
    enum class Mode { Login, Register }
}

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun setName(value: String) = _ui.update { it.copy(name = value, error = null) }
    fun setEmail(value: String) = _ui.update { it.copy(email = value, error = null) }
    fun setPassword(value: String) = _ui.update { it.copy(password = value, error = null) }

    fun toggleMode() = _ui.update {
        it.copy(
            mode = if (it.mode == LoginUiState.Mode.Login) LoginUiState.Mode.Register else LoginUiState.Mode.Login,
            error = null,
        )
    }

    fun submit(onSuccess: () -> Unit) {
        val state = _ui.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _ui.update { it.copy(error = "请填写邮箱和密码") }
            return
        }
        if (state.mode == LoginUiState.Mode.Register && state.name.isBlank()) {
            _ui.update { it.copy(error = "请填写称呼") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching {
                when (state.mode) {
                    LoginUiState.Mode.Login -> authRepository.login(state.email, state.password)
                    LoginUiState.Mode.Register -> authRepository.register(state.email, state.password, state.name)
                }
            }.onSuccess {
                _ui.update { it.copy(loading = false) }
                onSuccess()
            }.onFailure { err ->
                _ui.update { it.copy(loading = false, error = err.toUserMessage()) }
            }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(authRepository) as T
            }
        }
    }
}
