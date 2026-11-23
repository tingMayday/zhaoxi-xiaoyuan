package com.zhaoxi.xiaoyuan.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.auth.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SessionState(
    val ready: Boolean = false,
    val loggedIn: Boolean = false,
)

class SessionViewModel(
    authRepository: AuthRepository,
) : ViewModel() {
    val state: StateFlow<SessionState> = authRepository.tokenFlow
        .map { token -> SessionState(ready = true, loggedIn = !token.isNullOrBlank()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionState(),
        )

    companion object {
        fun factory(authRepository: AuthRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SessionViewModel(authRepository) as T
            }
        }
    }
}
