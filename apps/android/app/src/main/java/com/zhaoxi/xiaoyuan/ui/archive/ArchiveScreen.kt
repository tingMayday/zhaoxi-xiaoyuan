package com.zhaoxi.xiaoyuan.ui.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.common.PageHeader
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import java.time.LocalDate

@Composable
fun ArchiveScreen(viewModel: ArchiveViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val shown = remember(state.board, state.kind) {
        state.board?.days.orEmpty().mapNotNull { day ->
            val items = if (state.kind.isBlank()) day.items else day.items.filter { it.kind == state.kind }
            if (items.isEmpty()) null else day.copy(items = items)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PageHeader(eyebrow = "数据", title = "时光档案", subtitle = "所有日常，都有出处")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("" to "全部", "finance" to "财务", "health" to "健康", "schedule" to "日程", "buy" to "待买").forEach { (id, label) ->
                FilterChip(
                    selected = state.kind == id,
                    onClick = { viewModel.setKind(id) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Accent, selectedLabelColor = OnAccent),
                )
            }
        }

        when {
            state.loading && state.board == null ->
                CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
            state.error != null && state.board == null -> Text(state.error ?: "", color = Accent)
            shown.isEmpty() -> EmptyBox("这段时间还没有留下记录。")
            else -> shown.forEach { day ->
                val open = state.openDates.contains(day.date)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Card, RoundedCornerShape(16.dp))
                        .border(1.dp, Line, RoundedCornerShape(16.dp)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggle(day.date) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(dayTitle(day.date, state.board?.today.orEmpty()), style = MaterialTheme.typography.titleLarge)
                        Text("${day.items.size} 条记录", color = Muted)
                    }
                    if (open) {
                        day.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(kindLabel(item.kind), color = Muted, modifier = Modifier.weight(0.25f))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title)
                                    if (item.detail.isNotBlank()) Text(item.detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(item.value)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun dayTitle(date: String, today: String): String {
    if (date == today) return "今天"
    if (today.isNotBlank() && date == LocalDate.parse(today).minusDays(1).toString()) return "昨天"
    val parts = date.split("-")
    return if (parts.size == 3) "${parts[1].toInt()} 月 ${parts[2].toInt()} 日" else date
}
