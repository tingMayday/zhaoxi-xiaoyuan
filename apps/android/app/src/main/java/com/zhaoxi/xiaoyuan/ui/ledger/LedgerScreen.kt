package com.zhaoxi.xiaoyuan.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.data.ledger.Category
import com.zhaoxi.xiaoyuan.data.ledger.CategorySpend
import androidx.compose.material3.AlertDialog
import com.zhaoxi.xiaoyuan.data.ledger.FinanceSummary
import com.zhaoxi.xiaoyuan.ui.common.ActionButton
import com.zhaoxi.xiaoyuan.ui.common.ActionTone
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.ConfirmDialog
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.common.FieldKind
import com.zhaoxi.xiaoyuan.ui.common.MoneyValue
import com.zhaoxi.xiaoyuan.ui.common.PageHeader
import com.zhaoxi.xiaoyuan.ui.common.ToastMessage
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Ink
import com.zhaoxi.xiaoyuan.ui.theme.Lilac
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Paper
import com.zhaoxi.xiaoyuan.ui.theme.Sage
import com.zhaoxi.xiaoyuan.ui.theme.Sand
import com.zhaoxi.xiaoyuan.ui.util.yuan

@Composable
fun LedgerScreen(viewModel: LedgerViewModel) {
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
        PageHeader(eyebrow = "记账", title = "花得明白")

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { viewModel.shiftMonth(-1) }) { Text("上月", color = Accent) }
            Text(
                state.month,
                style = MaterialTheme.typography.titleLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { viewModel.shiftMonth(1) }) { Text("下月", color = Accent) }
        }

        if (state.loading && state.summary == null) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            state.summary?.let { summary ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    MiniStat(modifier = Modifier.weight(1f), "收入", "¥${yuan(summary.incomeCents)}", Sand)
                    MiniStat(modifier = Modifier.weight(1f), "支出", "¥${yuan(summary.expenseCents)}", Sage)
                    MiniStat(modifier = Modifier.weight(1f), "结余", "¥${yuan(summary.balanceCents)}", Lilac)
                }

                BudgetStrip(
                    summary = summary,
                    onEdit = viewModel::openBudgetDialog,
                )

                val spends = summary.categories.filter { it.amountCents > 0 }
                if (spends.isNotEmpty()) {
                    Text("消费结构", style = MaterialTheme.typography.headlineMedium)
                    SpendStructure(spends)
                }
            }

            Text("记一笔", style = MaterialTheme.typography.headlineMedium)
            DirectionPicker(
                direction = state.direction,
                onSelect = viewModel::setDirection,
            )
            AppField("日期", state.occurredOn, viewModel::setOccurredOn, kind = FieldKind.Date)
            AppField("金额", state.amount, viewModel::setAmount, kind = FieldKind.Decimal)
            Text("分类", color = Muted, style = MaterialTheme.typography.labelLarge)
            if (expenseCats.isEmpty()) {
                Text("暂无可用分类", color = Muted, style = MaterialTheme.typography.bodyMedium)
            } else {
                CategoryPicker(
                    categories = expenseCats,
                    selectedId = state.categoryId,
                    onSelect = viewModel::setCategoryId,
                )
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
                            val sign = if (tx.direction == "expense") "-" else "+"
                            MoneyValue(
                                value = "$sign¥${yuan(tx.amountCents)}",
                                label = tx.category.name,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        ActionButton(
                            label = "删除",
                            onClick = { viewModel.askDelete(tx) },
                            tone = ActionTone.Danger,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    ToastMessage(message = state.message, onConsumed = viewModel::clearMessage)

    if (state.budgetDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeBudgetDialog,
            title = { Text("设置月预算") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${state.month} 的总预算（元）", color = Muted)
                    AppField(
                        label = "预算金额",
                        value = state.budget,
                        onChange = viewModel::setBudget,
                        kind = FieldKind.Decimal,
                    )
                    state.error?.let { Text(it, color = Accent, style = MaterialTheme.typography.bodyMedium) }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::saveBudget,
                    enabled = !state.busy,
                ) { Text("保存", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeBudgetDialog) { Text("取消", color = Muted) }
            },
            containerColor = Card,
        )
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
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            label,
            color = Muted,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        MoneyValue(
            value = value,
            label = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BudgetStrip(summary: FinanceSummary, onEdit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("月预算", style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (summary.totalBudgetCents == null) "设置" else "修改",
                color = OnAccent,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Accent)
                    .clickable(onClick = onEdit)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
        when {
            summary.totalBudgetCents == null -> {
                Text("还没有设置本月预算", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BudgetCell(
                        modifier = Modifier.weight(1f),
                        label = "预算",
                        value = "¥${yuan(summary.totalBudgetCents)}",
                    )
                    BudgetCell(
                        modifier = Modifier.weight(1f),
                        label = "剩余",
                        value = summary.remainingCents?.let { "¥${yuan(it)}" } ?: "—",
                    )
                    BudgetCell(
                        modifier = Modifier.weight(1f),
                        label = "已用",
                        value = summary.usedRatio?.let { "${(it * 100).toInt()}%" } ?: "—",
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetCell(modifier: Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Paper.copy(alpha = 0.45f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        MoneyValue(
            value = value,
            label = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DirectionPicker(direction: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("expense" to "支出", "income" to "收入").forEach { (id, label) ->
            val selected = direction == id
            Text(
                text = label,
                color = if (selected) OnAccent else Accent,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) Accent else Card)
                    .border(1.dp, if (selected) Accent else Line, RoundedCornerShape(999.dp))
                    .clickable { onSelect(id) }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPicker(
    categories: List<Category>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { cat ->
            val selected = selectedId == cat.id
            Text(
                text = cat.name,
                color = if (selected) OnAccent else Ink,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) Accent else Paper.copy(alpha = 0.55f))
                    .border(
                        width = 1.dp,
                        color = if (selected) Accent else Line,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .clickable { onSelect(cat.id) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun SpendStructure(spends: List<CategorySpend>) {
    val max = spends.maxOf { it.amountCents }.coerceAtLeast(1)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        spends.sortedByDescending { it.amountCents }.forEach { cat ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        cat.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    MoneyValue(
                        value = "¥${yuan(cat.amountCents)}",
                        label = cat.name,
                        color = Muted,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Paper.copy(alpha = 0.7f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(cat.amountCents.toFloat() / max.toFloat())
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Accent.copy(alpha = 0.75f)),
                    )
                }
            }
        }
    }
}

