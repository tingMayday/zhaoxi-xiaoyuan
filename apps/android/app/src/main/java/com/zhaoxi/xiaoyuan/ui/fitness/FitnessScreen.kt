package com.zhaoxi.xiaoyuan.ui.fitness

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Blush
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Lilac
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Sage
import com.zhaoxi.xiaoyuan.ui.theme.Sand
import kotlin.math.roundToInt

private val weekdayLabels = listOf(
    1 to "周一", 2 to "周二", 3 to "周三", 4 to "周四", 5 to "周五", 6 to "周六", 7 to "周日",
)

@Composable
fun FitnessScreen(viewModel: FitnessViewModel, onBack: () -> Unit) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val series = state.series

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
        Text("健身", color = Muted, style = MaterialTheme.typography.labelLarge)
        Text("稳稳向前", style = MaterialTheme.typography.headlineLarge)

        if (state.loading && series == null) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Stat(modifier = Modifier.weight(1f), "当前体重", series?.latest?.weightKg?.let { "$it kg" } ?: "—", Sand)
                Stat(modifier = Modifier.weight(1f), "BMI", series?.bmi?.toString() ?: "—", Sage)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                val progress = series?.progress?.let { "${(it * 100).roundToInt()}%" } ?: "—"
                Stat(modifier = Modifier.weight(1f), "目标进度", progress, Lilac)
                Stat(modifier = Modifier.weight(1f), "热量缺口", series?.deficit?.let { "$it kcal" } ?: "—", Blush)
            }

            Text("今天记下", style = MaterialTheme.typography.headlineMedium)
            AppField("体重 kg", state.weight, viewModel::setWeight)
            AppField("摄入 kcal", state.intake, viewModel::setIntake)
            AppField("运动分钟", state.minutes, viewModel::setMinutes)
            state.error?.let { Text(it, color = Accent) }
            Button(
                onClick = viewModel::save,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) { Text("保存") }

            Text("减脂目标", style = MaterialTheme.typography.headlineMedium)
            AppField("起点体重 kg", state.startWeight, viewModel::setStartWeight)
            AppField("目标体重 kg", state.targetWeight, viewModel::setTargetWeight)
            AppField("开始日期 YYYY-MM-DD", state.startDate, viewModel::setStartDate)
            AppField("每日摄入目标 kcal", state.targetIntake, viewModel::setTargetIntake)
            Button(
                onClick = viewModel::saveProfile,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) { Text("保存目标") }

            Text("一周安排", style = MaterialTheme.typography.headlineMedium)
            weekdayLabels.forEach { (day, label) ->
                AppField(label, state.planDraft[day].orEmpty(), onChange = { viewModel.setPlanDay(day, it) })
            }
            Button(
                onClick = viewModel::saveWeekPlan,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) { Text("保存一周") }

            Text("最近体重", style = MaterialTheme.typography.headlineMedium)
            val points = series?.points?.filter { it.weightKg != null }?.takeLast(10).orEmpty()
            if (points.isEmpty()) {
                EmptyBox("还没有体重。记下今天的数字，曲线会从这里长出来。")
            } else {
                points.asReversed().forEach { point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Card, RoundedCornerShape(12.dp))
                            .border(1.dp, Line, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(point.date)
                        Text("${point.weightKg} kg")
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(modifier: Modifier, label: String, value: String, bg: androidx.compose.ui.graphics.Color) {
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
