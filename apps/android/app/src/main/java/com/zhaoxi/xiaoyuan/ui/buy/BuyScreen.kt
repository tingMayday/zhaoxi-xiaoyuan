package com.zhaoxi.xiaoyuan.ui.buy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.data.buy.BuyBoard
import com.zhaoxi.xiaoyuan.data.buy.BuyItem
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.ConfirmDialog
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.common.PageHeader
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Blush
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
fun BuyScreen(viewModel: BuyViewModel, onBack: () -> Unit) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val open = viewModel.openItems()
    val bought = viewModel.boughtItems()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader(onBack = onBack, eyebrow = "待买", title = "想到就记下")

        if (state.loading && state.board == null) {
            CircularProgressIndicator(
                color = Accent,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(48.dp),
            )
        } else {
            state.board?.let { board ->
                HeroCard(board)
                GlanceRow(board)
            }

            SectionCard(title = "加入清单", hint = "名称必填，金额和数量可选") {
                AppField("名称", state.title, viewModel::setTitle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AppField(
                        "数量",
                        state.quantity,
                        viewModel::setQuantity,
                        modifier = Modifier.weight(0.35f),
                    )
                    AppField(
                        "预估金额",
                        state.amount,
                        viewModel::setAmount,
                        modifier = Modifier.weight(0.65f),
                    )
                }
                AppField("备注", state.note, viewModel::setNote)
                state.error?.let {
                    Text(it, color = Accent, style = MaterialTheme.typography.bodyMedium)
                }
                PrimaryButton(
                    label = "加入清单",
                    enabled = !state.busy,
                    onClick = viewModel::create,
                )
            }

            SectionCard(
                title = "还没买",
                hint = if (open.isEmpty()) "清单空着" else "${open.size} 件在等",
            ) {
                if (open.isEmpty()) {
                    EmptyBox("清单是空的。想到要买的，先写下来。")
                } else {
                    open.forEach { item ->
                        BuyRow(
                            item = item,
                            bought = false,
                            onBuy = { viewModel.askBuy(item) },
                            onRemove = { viewModel.askDelete(item) },
                        )
                    }
                }
            }

            SectionCard(
                title = "本月已买",
                hint = if (bought.isEmpty()) "还没有记下" else "${bought.size} 件到手",
            ) {
                if (bought.isEmpty()) {
                    EmptyBox("这个月还没有记下买到的东西。")
                } else {
                    bought.forEach { item ->
                        BuyRow(
                            item = item,
                            bought = true,
                            onBuy = null,
                            onRemove = { viewModel.askDelete(item) },
                        )
                    }
                }
            }
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
private fun HeroCard(board: BuyBoard) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Paper.copy(alpha = 0.5f))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("清单概览", color = Accent, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (board.openCount == 0) "清单清空了" else "还有 ${board.openCount} 件想买",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    if (board.estimateCents > 0) {
                        "预估合计 ¥${yuan(board.estimateCents)}"
                    } else {
                        "还没写预估金额"
                    },
                    color = Muted,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${board.boughtThisMonth}",
                    style = MaterialTheme.typography.displayLarge,
                    color = Accent,
                )
                Text("本月已买", color = Muted, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun GlanceRow(board: BuyBoard) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MiniGlance(
            modifier = Modifier.weight(1f),
            label = "未买",
            value = "${board.openCount}",
            bg = Sand,
        )
        MiniGlance(
            modifier = Modifier.weight(1f),
            label = "预估",
            value = "¥${yuan(board.estimateCents)}",
            bg = Blush,
        )
        MiniGlance(
            modifier = Modifier.weight(1f),
            label = "本月买",
            value = "${board.boughtThisMonth}",
            bg = Sage,
        )
        MiniGlance(
            modifier = Modifier.weight(1f),
            label = "清单",
            value = "${board.items.size}",
            bg = Lilac,
        )
    }
}

@Composable
private fun MiniGlance(
    modifier: Modifier,
    label: String,
    value: String,
    bg: androidx.compose.ui.graphics.Color,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label,
            color = Muted,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    hint: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            if (hint.isNotBlank()) {
                Text(hint, color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
        content()
    }
}

@Composable
private fun BuyRow(
    item: BuyItem,
    bought: Boolean,
    onBuy: (() -> Unit)?,
    onRemove: (() -> Unit)?,
) {
    val money = yuan(Math.round(item.estimateCents * item.quantity).toInt())
    val qty = item.quantity.toInt().takeIf { item.quantity % 1.0 == 0.0 } ?: item.quantity

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (bought) Sage.copy(alpha = 0.45f) else Paper.copy(alpha = 0.4f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (bought) Sage else Accent.copy(alpha = 0.7f)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    item.title,
                    color = if (bought) Muted else Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    buildString {
                        append("×$qty")
                        if (item.note.isNotBlank()) append(" · ${item.note}")
                        if (bought && !item.boughtOn.isNullOrBlank()) {
                            append(" · ${item.boughtOn}")
                        }
                    },
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                "¥$money",
                style = MaterialTheme.typography.titleMedium,
                color = if (bought) Muted else Ink,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBuy != null) {
                PillButton(
                    label = "买到了",
                    filled = true,
                    onClick = onBuy,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Text(
                    "已买到",
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Card)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            if (onRemove != null) {
                PillButton(
                    label = "删除",
                    filled = false,
                    onClick = onRemove,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PillButton(
    label: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (filled) Accent else Card)
            .border(1.dp, if (filled) Accent else Line, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (filled) OnAccent else Muted,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = OnAccent,
            disabledContainerColor = Accent.copy(alpha = 0.35f),
            disabledContentColor = OnAccent.copy(alpha = 0.8f),
        ),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
