package com.zhaoxi.xiaoyuan.ui.buy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.data.buy.BuyItem
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.ConfirmDialog
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.util.yuan

@Composable
fun BuyScreen(viewModel: BuyViewModel, onBack: () -> Unit) {
    val state by viewModel.ui.collectAsStateWithLifecycle()

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
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("返回", color = Accent) }
        Text("待买", color = Muted, style = MaterialTheme.typography.labelLarge)
        Text("想到就记下", style = MaterialTheme.typography.headlineLarge)

        if (state.loading && state.board == null) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            state.board?.let { board ->
                Text(
                    "未买 ${board.openCount} 件 · 预估 ¥${yuan(board.estimateCents)} · 本月已买 ${board.boughtThisMonth}",
                    color = Muted,
                )
            }
            AppField("名称", state.title, viewModel::setTitle)
            AppField("数量", state.quantity, viewModel::setQuantity)
            AppField("预估金额", state.amount, viewModel::setAmount)
            AppField("备注", state.note, viewModel::setNote)
            state.error?.let { Text(it, color = Accent) }
            Button(
                onClick = viewModel::create,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) { Text("加入清单") }

            Text("还没买", style = MaterialTheme.typography.headlineMedium)
            val open = viewModel.openItems()
            if (open.isEmpty()) EmptyBox("清单是空的。想到要买的，先写下来。")
            else open.forEach { item ->
                BuyRow(
                    item = item,
                    buy = { viewModel.askBuy(item) },
                    remove = { viewModel.askDelete(item) },
                )
            }

            Text("本月已买", style = MaterialTheme.typography.headlineMedium)
            val bought = viewModel.boughtItems()
            if (bought.isEmpty()) EmptyBox("这个月还没有记下买到的东西。")
            else bought.forEach { item -> BuyRow(item, buy = null, remove = { viewModel.askDelete(item) }) }
        }
    }

    state.pendingBuy?.let { item ->
        ConfirmDialog(
            title = "标为买到",
            message = "把「${item.title}」标成已经买到？",
            confirmText = "买到了",
            onConfirm = viewModel::confirmBuy,
            onDismiss = viewModel::cancelDialog,
        )
    }
    state.pendingDelete?.let { item ->
        ConfirmDialog(
            title = "删除待买",
            message = "删除后，「${item.title}」就不会再出现。",
            confirmText = "删除",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDialog,
        )
    }
}

@Composable
private fun BuyRow(item: BuyItem, buy: (() -> Unit)?, remove: (() -> Unit)?) {
    val money = yuan(Math.round(item.estimateCents * item.quantity).toInt())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(14.dp))
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title)
            val qty = item.quantity.toInt().takeIf { item.quantity % 1.0 == 0.0 } ?: item.quantity
            Text(
                buildString {
                    append("×$qty")
                    if (item.note.isNotBlank()) append(" · ${item.note}")
                },
                color = Muted,
            )
        }
        Text("¥$money")
        buy?.let { TextButton(onClick = it) { Text("买到了", color = Accent) } }
        remove?.let { TextButton(onClick = it) { Text("删除", color = Muted) } }
    }
}
