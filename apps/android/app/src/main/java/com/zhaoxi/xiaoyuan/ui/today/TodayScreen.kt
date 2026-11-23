package com.zhaoxi.xiaoyuan.ui.today

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Blush
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Lilac
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.Sage
import com.zhaoxi.xiaoyuan.ui.theme.Sand
import com.zhaoxi.xiaoyuan.ui.util.yuan

@Composable
fun TodayScreen(viewModel: TodayViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        when {
            state.loading && state.data == null -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(48.dp),
                    color = Accent,
                )
            }
            state.error != null && state.data == null -> {
                Text(state.error ?: "", color = Accent)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = viewModel::refresh,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text("再试一次")
                }
            }
            else -> {
                val data = state.data ?: return
                Text(data.date, color = Muted, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Text("今天，慢慢来", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${data.score}", style = MaterialTheme.typography.displayLarge)
                    Text(" / 100", color = Muted, modifier = Modifier.padding(bottom = 10.dp, start = 4.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(data.quote, color = Muted, style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "本月支出",
                        value = "¥${yuan(data.finance.expenseCents)}",
                        hint = data.finance.remainingCents?.let { "预算还剩 ¥${yuan(it)}" } ?: "还没有月预算",
                        background = Sand,
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "今日习惯",
                        value = "${data.habits.done} / ${data.habits.total}",
                        hint = if (data.habits.total == 0) "还没有习惯" else "从一件小事开始",
                        background = Sage,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "今日待办",
                        value = "${data.schedule.todayOpen}",
                        hint = "逾期 ${data.schedule.overdue} · 未来 7 天 ${data.schedule.next7}",
                        background = Lilac,
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "最近体重",
                        value = data.fitness.weightKg?.let { "$it kg" } ?: "—",
                        hint = data.fitness.weightDate ?: "还没有记录",
                        background = Blush,
                    )
                }

                state.error?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(it, color = Accent)
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    hint: String,
    background: androidx.compose.ui.graphics.Color,
) {
    Column(
        modifier = modifier
            .background(background, RoundedCornerShape(18.dp))
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(hint, color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}
