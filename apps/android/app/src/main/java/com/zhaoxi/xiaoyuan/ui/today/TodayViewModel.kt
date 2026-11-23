package com.zhaoxi.xiaoyuan.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import com.zhaoxi.xiaoyuan.data.today.TodayOverview
import com.zhaoxi.xiaoyuan.data.today.TodayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class TodayUiState(
    val loading: Boolean = true,
    val data: TodayOverview? = null,
    val error: String? = null,
    val unauthorized: Boolean = false,
)

class TodayViewModel(
    private val todayRepository: TodayRepository,
    private val onUnauthorized: suspend () -> Unit,
) : ViewModel() {
    private val _ui = MutableStateFlow(TodayUiState())
    val ui: StateFlow<TodayUiState> = _ui.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val keepShowing = _ui.value.data != null
            _ui.update {
                it.copy(
                    loading = !keepShowing,
                    error = null,
                    unauthorized = false,
                )
            }
            runCatching { todayRepository.fetch() }
                .onSuccess { overview ->
                    _ui.update { it.copy(loading = false, data = overview, error = null) }
                }
                .onFailure { err ->
                    if (err is HttpException && err.code() == 401) {
                        onUnauthorized()
                        _ui.update { it.copy(loading = false, unauthorized = true, error = "请重新登录") }
                    } else {
                        _ui.update { it.copy(loading = false, error = err.toUserMessage()) }
                    }
                }
        }
    }

    companion object {
        fun factory(
            todayRepository: TodayRepository,
            onUnauthorized: suspend () -> Unit,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TodayViewModel(todayRepository, onUnauthorized) as T
            }
        }
    }
}
