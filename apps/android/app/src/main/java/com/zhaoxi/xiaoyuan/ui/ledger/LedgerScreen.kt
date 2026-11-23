package com.zhaoxi.xiaoyuan.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.ConfirmDialog
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Lilac
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Sage
import com.zhaoxi.xiaoyuan.ui.theme.Sand
import com.zhaoxi.xiaoyuan.ui.util.yuan

@Composable
fun LedgerScreen(viewModel: LedgerViewModel, onBack: () -> Unit) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val expenseCats = state.categories.filter { it.kind == state.direction }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("返回首页", color = Accent) }
        Text("记账", color = Muted, style = MaterialTheme.typography.labelLarge)
        Text("花得明白", style = MaterialTheme.typography.headlineLarge)

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { viewModel.shiftMonth(-1) }) { Text("上月", color = Accent) }
            Text(state.month, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 8.dp))
            TextButton(onClick = { viewModel.shiftMonth(1) }) { Text("下月", color = Accent) }
        }

        if (state.loading && state.summary == null) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            state.summary?.let { summary ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MiniStat(modifier = Modifier.weight(1f), "收入", "¥${yuan(summary.incomeCents)}", Sand)
                    MiniStat(modifier = Modifier.weight(1f), "支出", "¥${yuan(summary.expenseCents)}", Sage)
                    MiniStat(modifier = Modifier.weight(1f), "结余", "¥${yuan(summary.balanceCents)}", Lilac)
                }
                val budgetHint = when {
                    summary.totalBudgetCents == null -> "还没有月预算"
                    summary.remainingCents != null -> "剩余 ¥${yuan(summary.remainingCents)} · 已用 ${((summary.usedRatio ?: 0.0) * 100).toInt()}%"
                    else -> "月预算 ¥${yuan(summary.totalBudgetCents)}"
                }
                Text(budgetHint, color = Muted)
                AppField("本月总预算", state.budget, viewModel::setBudget)
                Button(
                    onClick = viewModel::saveBudget,
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
                ) { Text("保存预算") }

                val spends = summary.categories.filter { it.amountCents > 0 }.take(6)
                if (spends.isNotEmpty()) {
                    Text("消费结构", style = MaterialTheme.typography.headlineMedium)
                    spends.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Card, RoundedCornerShape(12.dp))
                                .border(1.dp, Line, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(cat.name)
                            Text("¥${yuan(cat.amountCents)}")
                        }
                    }
                }
            }

            Text("记一笔", style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("expense" to "支出", "income" to "收入").forEach { (id, label) ->
                    FilterChip(
                        selected = state.direction == id,
                        onClick = { viewModel.setDirection(id) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accent,
                            selectedLabelColor = OnAccent,
                        ),
                    )
                }
            }
            AppField("日期 YYYY-MM-DD", state.occurredOn, viewModel::setOccurredOn)
            AppField("金额", state.amount, viewModel::setAmount)
            if (expenseCats.isNotEmpty()) {
                Text("分类", color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    expenseCats.take(6).forEach { cat ->
                        FilterChip(
                            selected = state.categoryId == cat.id,
                            onClick = { viewModel.setCategoryId(cat.id) },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Accent,
                                selectedLabelColor = OnAccent,
                            ),
                        )
                    }
                }
            }
            AppField("备注", state.note, viewModel::setNote)
            state.error?.let { Text(it, color = Accent) }
            Button(
                onClick = viewModel::create,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) { Text("记下") }

            Text("流水", style = MaterialTheme.typography.headlineMedium)
            if (state.transactions.isEmpty()) {
                EmptyBox("这组筛选下还没有流水。")
            } else {
                state.transactions.forEach { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Card, RoundedCornerShape(14.dp))
                            .border(1.dp, Line, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${tx.category.name} · ${tx.occurredOn.takeLast(5)}")
                            if (tx.note.isNotBlank()) Text(tx.note, color = Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                        val sign = if (tx.direction == "expense") "-" else "+"
                        Text("$sign¥${yuan(tx.amountCents)}", style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = { viewModel.askDelete(tx) }) { Text("删除", color = Accent) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    state.pendingDelete?.let { item ->
        ConfirmDialog(
            title = "删除流水",
            message = "删除后，这笔「${item.category.name}」记录就不会再出现。",
            confirmText = "删除",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }
}

@Composable
private fun MiniStat(modifier: Modifier, label: String, value: String, bg: androidx.compose.ui.graphics.Color) {
    Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(16.dp))
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}
