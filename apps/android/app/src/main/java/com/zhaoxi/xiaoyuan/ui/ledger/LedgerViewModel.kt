package com.zhaoxi.xiaoyuan.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.ledger.Category
import com.zhaoxi.xiaoyuan.data.ledger.FinanceSummary
import com.zhaoxi.xiaoyuan.data.ledger.LedgerRepository
import com.zhaoxi.xiaoyuan.data.ledger.NewTransaction
import com.zhaoxi.xiaoyuan.data.ledger.Transaction
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import com.zhaoxi.xiaoyuan.ui.common.currentMonth
import com.zhaoxi.xiaoyuan.ui.common.todayDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LedgerUiState(
    val month: String = currentMonth(),
    val loading: Boolean = true,
    val summary: FinanceSummary? = null,
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val direction: String = "expense",
    val amount: String = "",
    val note: String = "",
    val categoryId: String = "",
    val occurredOn: String = todayDate(),
    val budget: String = "",
    val budgetDialogOpen: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val busy: Boolean = false,
    val pendingDelete: Transaction? = null,
)

class LedgerViewModel(
    private val repo: LedgerRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(LedgerUiState())
    val ui: StateFlow<LedgerUiState> = _ui.asStateFlow()

    fun shiftMonth(delta: Long) {
        val next = com.zhaoxi.xiaoyuan.ui.common.shiftMonth(_ui.value.month, delta)
        _ui.update { it.copy(month = next) }
        refresh()
    }

    fun setDirection(value: String) = _ui.update {
        val cats = it.categories.filter { c -> c.kind == value }
        it.copy(direction = value, categoryId = cats.firstOrNull()?.id.orEmpty(), error = null)
    }

    fun setAmount(value: String) = _ui.update { it.copy(amount = value, error = null) }
    fun setNote(value: String) = _ui.update { it.copy(note = value) }
    fun setCategoryId(value: String) = _ui.update { it.copy(categoryId = value) }
    fun setOccurredOn(value: String) = _ui.update { it.copy(occurredOn = value) }
    fun setBudget(value: String) = _ui.update { it.copy(budget = value, error = null) }
    fun openBudgetDialog() = _ui.update { it.copy(budgetDialogOpen = true, error = null) }
    fun closeBudgetDialog() = _ui.update { it.copy(budgetDialogOpen = false) }
    fun clearMessage() = _ui.update { it.copy(message = null) }
    fun askDelete(item: Transaction) = _ui.update { it.copy(pendingDelete = item) }
    fun cancelDelete() = _ui.update { it.copy(pendingDelete = null) }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching {
                val month = _ui.value.month
                Triple(repo.summary(month), repo.transactions(month), repo.categories())
            }.onSuccess { (summary, txs, cats) ->
                val direction = _ui.value.direction
                val filtered = cats.filter { it.kind == direction }
                _ui.update {
                    it.copy(
                        loading = false,
                        summary = summary,
                        transactions = txs,
                        categories = cats,
                        categoryId = it.categoryId.ifBlank { filtered.firstOrNull()?.id.orEmpty() },
                        budget = summary.totalBudgetCents?.let { cents ->
                            if (cents % 100 == 0) (cents / 100).toString() else (cents / 100.0).toString()
                        }.orEmpty(),
                    )
                }
            }.onFailure { err ->
                _ui.update { it.copy(loading = false, error = err.toUserMessage()) }
            }
        }
    }

    fun saveBudget() {
        val state = _ui.value
        val yuan = state.budget.trim().toDoubleOrNull()
        if (yuan == null || yuan < 0) {
            _ui.update { it.copy(error = "请填写有效预算金额") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            runCatching {
                repo.upsertBudget(
                    com.zhaoxi.xiaoyuan.data.ledger.UpsertBudget(
                        yearMonth = state.month,
                        categoryKey = "total",
                        amountCents = Math.round(yuan * 100).toInt(),
                    ),
                )
            }.onSuccess {
                _ui.update {
                    it.copy(
                        busy = false,
                        budgetDialogOpen = false,
                        message = "预算已保存",
                    )
                }
                refresh()
            }.onFailure { err ->
                _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
            }
        }
    }

    fun create() {
        val state = _ui.value
        val yuan = state.amount.toDoubleOrNull()
        if (yuan == null || yuan <= 0) {
            _ui.update { it.copy(error = "请填写大于 0 的金额") }
            return
        }
        if (state.categoryId.isBlank()) {
            _ui.update { it.copy(error = "请选择分类") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            runCatching {
                repo.create(
                    NewTransaction(
                        occurredOn = state.occurredOn,
                        direction = state.direction,
                        amountCents = Math.round(yuan * 100).toInt(),
                        categoryId = state.categoryId,
                        note = state.note.trim(),
                    ),
                )
            }.onSuccess {
                _ui.update { it.copy(busy = false, amount = "", note = "") }
                refresh()
            }.onFailure { err ->
                _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
            }
        }
    }

    fun confirmDelete() {
        val item = _ui.value.pendingDelete ?: return
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, pendingDelete = null, error = null) }
            runCatching { repo.remove(item.id) }
                .onSuccess {
                    _ui.update { it.copy(busy = false) }
                    refresh()
                }
                .onFailure { err ->
                    _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
                }
        }
    }

    companion object {
        fun factory(repo: LedgerRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = LedgerViewModel(repo) as T
        }
    }
}
