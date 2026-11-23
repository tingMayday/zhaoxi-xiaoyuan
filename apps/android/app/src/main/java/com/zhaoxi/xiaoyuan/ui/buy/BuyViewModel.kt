package com.zhaoxi.xiaoyuan.ui.buy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.buy.BuyBoard
import com.zhaoxi.xiaoyuan.data.buy.BuyItem
import com.zhaoxi.xiaoyuan.data.buy.BuyRepository
import com.zhaoxi.xiaoyuan.data.buy.NewBuyItem
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import com.zhaoxi.xiaoyuan.ui.common.currentMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BuyUiState(
    val loading: Boolean = true,
    val board: BuyBoard? = null,
    val title: String = "",
    val amount: String = "",
    val quantity: String = "1",
    val note: String = "",
    val error: String? = null,
    val busy: Boolean = false,
    val pendingBuy: BuyItem? = null,
    val pendingDelete: BuyItem? = null,
)

class BuyViewModel(private val repo: BuyRepository) : ViewModel() {
    private val _ui = MutableStateFlow(BuyUiState())
    val ui: StateFlow<BuyUiState> = _ui.asStateFlow()
    private val month = currentMonth()

    fun setTitle(v: String) = _ui.update { it.copy(title = v, error = null) }
    fun setAmount(v: String) = _ui.update { it.copy(amount = v) }
    fun setQuantity(v: String) = _ui.update { it.copy(quantity = v) }
    fun setNote(v: String) = _ui.update { it.copy(note = v) }
    fun askBuy(item: BuyItem) = _ui.update { it.copy(pendingBuy = item) }
    fun askDelete(item: BuyItem) = _ui.update { it.copy(pendingDelete = item) }
    fun cancelDialog() = _ui.update { it.copy(pendingBuy = null, pendingDelete = null) }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.board() }
                .onSuccess { board -> _ui.update { it.copy(loading = false, board = board) } }
                .onFailure { err -> _ui.update { it.copy(loading = false, error = err.toUserMessage()) } }
        }
    }

    fun create() {
        val state = _ui.value
        if (state.title.isBlank()) {
            _ui.update { it.copy(error = "请填写名称") }
            return
        }
        val qty = state.quantity.trim().toDoubleOrNull() ?: 1.0
        if (qty <= 0) {
            _ui.update { it.copy(error = "数量需大于 0") }
            return
        }
        val cents = state.amount.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()?.let { Math.round(it * 100).toInt() } ?: 0
        if (state.amount.isNotBlank() && cents < 0) {
            _ui.update { it.copy(error = "金额无效") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            runCatching {
                repo.create(
                    NewBuyItem(
                        title = state.title.trim(),
                        quantity = qty,
                        estimateCents = cents,
                        note = state.note.trim(),
                    ),
                )
            }.onSuccess {
                _ui.update { it.copy(busy = false, title = "", amount = "", quantity = "1", note = "") }
                refresh()
            }.onFailure { err -> _ui.update { it.copy(busy = false, error = err.toUserMessage()) } }
        }
    }

    fun confirmBuy() {
        val item = _ui.value.pendingBuy ?: return
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, pendingBuy = null, error = null) }
            runCatching { repo.markBought(item.id) }
                .onSuccess { _ui.update { it.copy(busy = false) }; refresh() }
                .onFailure { err -> _ui.update { it.copy(busy = false, error = err.toUserMessage()) } }
        }
    }

    fun confirmDelete() {
        val item = _ui.value.pendingDelete ?: return
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, pendingDelete = null, error = null) }
            runCatching { repo.remove(item.id) }
                .onSuccess { _ui.update { it.copy(busy = false) }; refresh() }
                .onFailure { err -> _ui.update { it.copy(busy = false, error = err.toUserMessage()) } }
        }
    }

    fun openItems() = _ui.value.board?.items?.filter { it.status == "open" }.orEmpty()
    fun boughtItems() = _ui.value.board?.items
        ?.filter { it.status == "bought" && it.boughtOn?.startsWith(month) == true }
        .orEmpty()

    companion object {
        fun factory(repo: BuyRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = BuyViewModel(repo) as T
        }
    }
}
