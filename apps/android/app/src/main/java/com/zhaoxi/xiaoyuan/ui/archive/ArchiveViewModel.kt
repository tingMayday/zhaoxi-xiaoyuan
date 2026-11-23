package com.zhaoxi.xiaoyuan.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.archive.ArchiveBoard
import com.zhaoxi.xiaoyuan.data.archive.ArchiveRepository
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArchiveUiState(
    val loading: Boolean = true,
    val board: ArchiveBoard? = null,
    val kind: String = "",
    val openDates: Set<String> = emptySet(),
    val error: String? = null,
)

class ArchiveViewModel(private val repo: ArchiveRepository) : ViewModel() {
    private val _ui = MutableStateFlow(ArchiveUiState())
    val ui: StateFlow<ArchiveUiState> = _ui.asStateFlow()

    init { refresh() }

    fun setKind(kind: String) = _ui.update { it.copy(kind = kind) }

    fun toggle(date: String) = _ui.update {
        val next = it.openDates.toMutableSet()
        if (!next.add(date)) next.remove(date)
        it.copy(openDates = next)
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.board() }
                .onSuccess { board ->
                    _ui.update {
                        it.copy(
                            loading = false,
                            board = board,
                            openDates = board.days.take(3).map { day -> day.date }.toSet(),
                        )
                    }
                }
                .onFailure { err ->
                    _ui.update { it.copy(loading = false, error = err.toUserMessage()) }
                }
        }
    }

    companion object {
        fun factory(repo: ArchiveRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ArchiveViewModel(repo) as T
        }
    }
}

fun kindLabel(kind: String) = when (kind) {
    "finance" -> "财务"
    "health" -> "健康"
    "schedule" -> "日程"
    "buy" -> "待买"
    else -> kind
}
