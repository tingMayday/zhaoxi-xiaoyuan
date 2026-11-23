package com.zhaoxi.xiaoyuan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.auth.AuthRepository
import com.zhaoxi.xiaoyuan.data.auth.UpdateProfileRequest
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loading: Boolean = true,
    val email: String = "",
    val name: String = "",
    val timezone: String = "Asia/Shanghai",
    val height: String = "",
    val error: String? = null,
    val message: String? = null,
    val busy: Boolean = false,
)

class SettingsViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui.asStateFlow()

    init { refresh() }

    fun setName(v: String) = _ui.update { it.copy(name = v, message = null, error = null) }
    fun setTimezone(v: String) = _ui.update { it.copy(timezone = v, message = null) }
    fun setHeight(v: String) = _ui.update { it.copy(height = v, message = null) }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { authRepository.profile() }
                .onSuccess { profile ->
                    _ui.update {
                        it.copy(
                            loading = false,
                            email = profile.email,
                            name = profile.name,
                            timezone = profile.timezone,
                            height = profile.heightCm?.toString().orEmpty(),
                        )
                    }
                }
                .onFailure { err ->
                    _ui.update { it.copy(loading = false, error = err.toUserMessage()) }
                }
        }
    }

    fun save() {
        val state = _ui.value
        val height = state.height.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
        if (state.height.isNotBlank() && height == null) {
            _ui.update { it.copy(error = "身高请填写数字") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null, message = null) }
            runCatching {
                authRepository.updateProfile(
                    UpdateProfileRequest(
                        name = state.name.trim(),
                        timezone = state.timezone.trim().ifBlank { "Asia/Shanghai" },
                        heightCm = height,
                    ),
                )
            }.onSuccess {
                _ui.update { it.copy(busy = false, message = "已保存") }
            }.onFailure { err ->
                _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
            }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(authRepository) as T
        }
    }
}
