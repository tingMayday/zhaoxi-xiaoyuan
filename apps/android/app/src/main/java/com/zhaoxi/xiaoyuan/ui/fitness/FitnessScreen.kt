package com.zhaoxi.xiaoyuan.ui.fitness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.data.fitness.WeightPoint
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.common.PageHeader
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Blush
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Lilac
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Paper
import com.zhaoxi.xiaoyuan.ui.theme.Sage
import com.zhaoxi.xiaoyuan.ui.theme.Sand
import kotlin.math.roundToInt

private val weekdayLabels = listOf(
    1 to "一", 2 to "二", 3 to "三", 4 to "四", 5 to "五", 6 to "六", 7 to "日",
)

@Composable
fun FitnessScreen(viewModel: FitnessViewModel) {
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader(eyebrow = "健身", title = "稳稳向前")

        if (state.loading && series == null) {
            CircularProgressIndicator(
                color = Accent,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(48.dp),
            )
        } else {
            HeroCard(state)

            GlanceRow(state)

            SectionCard(title = "今天记下", hint = series?.today?.let { "日期 $it" } ?: "") {
                AppField("体重 kg", state.weight, viewModel::setWeight, kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Decimal)
                AppField("摄入 kcal", state.intake, viewModel::setIntake, kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Number)
                AppField("运动分钟", state.minutes, viewModel::setMinutes, kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Number)
                state.error?.let { Text(it, color = Accent, style = MaterialTheme.typography.bodyMedium) }
                PrimaryButton("保存今日", enabled = !state.busy, onClick = viewModel::save)
            }

            SectionCard(
                title = "减脂目标",
                hint = series?.profile?.let {
                    "从 ${trimNum(it.startWeightKg)} kg 到 ${trimNum(it.targetWeightKg)} kg"
                } ?: "先定下起点和终点",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AppField(
                        "起点 kg",
                        state.startWeight,
                        viewModel::setStartWeight,
                        modifier = Modifier.weight(1f),
                        kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Decimal,
                    )
                    AppField(
                        "目标 kg",
                        state.targetWeight,
                        viewModel::setTargetWeight,
                        modifier = Modifier.weight(1f),
                        kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Decimal,
                    )
                }
                AppField("开始日期", state.startDate, viewModel::setStartDate, kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Date)
                AppField(
                    "每日摄入目标 kcal",
                    state.targetIntake,
                    viewModel::setTargetIntake,
                    kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Number,
                )
                PrimaryButton("保存目标", enabled = !state.busy, onClick = viewModel::saveProfile)
            }

            SectionCard(title = "一周安排", hint = "每天写一项安排，多项用 / 分隔") {
                weekdayLabels.forEach { (day, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Paper.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(label, color = Accent, style = MaterialTheme.typography.labelLarge)
                        }
                        AppField(
                            label = "周$label",
                            value = state.planDraft[day].orEmpty(),
                            onChange = { viewModel.setPlanDay(day, it) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                PrimaryButton("保存一周", enabled = !state.busy, onClick = viewModel::saveWeekPlan)
            }

            SectionCard(title = "体重走势", hint = "最近记录") {
                val points = series?.points?.filter { it.weightKg != null }.orEmpty()
                if (points.isEmpty()) {
                    EmptyBox("还没有体重。记下今天的数字，走势会从这里长出来。")
                } else {
                    WeightSparkline(points = points.takeLast(14))
                    Spacer(Modifier.height(8.dp))
                    points.takeLast(7).asReversed().forEach { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(point.date.takeLast(5), color = Muted)
                            Text("${trimNum(point.weightKg!!)} kg")
                            Text(
                                point.ma7?.let { "均 ${trimNum(it)}" } ?: "—",
                                color = Muted,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(state: FitnessUiState) {
    val series = state.series
    val progress = (series?.progress ?: 0.0).coerceIn(0.0, 1.0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paper.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("当前体重", color = Muted, style = MaterialTheme.typography.labelLarge)
                Text(
                    series?.latest?.weightKg?.let { "${trimNum(it)} kg" } ?: "—",
                    style = MaterialTheme.typography.displayLarge,
                    color = Accent,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("BMI", color = Muted, style = MaterialTheme.typography.labelLarge)
                Text(series?.bmi?.toString() ?: "—", style = MaterialTheme.typography.headlineMedium)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("目标进度 ${(progress * 100).roundToInt()}%", color = Muted)
                Text(
                    series?.deficit?.let { "缺口 $it kcal" } ?: "缺口 —",
                    color = Muted,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Card),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.toFloat())
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Accent),
                )
            }
        }
    }
}

@Composable
private fun GlanceRow(state: FitnessUiState) {
    val series = state.series
    val exercise = series?.todayLog?.exerciseMin
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MiniGlance(modifier = Modifier.weight(1f), "摄入目标", state.targetIntake.ifBlank { "—" }, Sand)
        MiniGlance(
            modifier = Modifier.weight(1f),
            "今日运动",
            exercise?.let { "$it 分" } ?: "—",
            Sage,
        )
        MiniGlance(
            modifier = Modifier.weight(1f),
            "目标体重",
            state.targetWeight.ifBlank { "—" }.let { if (it == "—") it else "$it kg" },
            Blush,
        )
        MiniGlance(
            modifier = Modifier.weight(1f),
            "进度",
            series?.progress?.let { "${(it * 100).roundToInt()}%" } ?: "—",
            Lilac,
        )
    }
}

@Composable
private fun MiniGlance(modifier: Modifier, label: String, value: String, bg: androidx.compose.ui.graphics.Color) {
    Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        com.zhaoxi.xiaoyuan.ui.common.MoneyValue(
            value = value,
            label = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SectionCard(title: String, hint: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(18.dp))
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            if (hint.isNotBlank()) Text(hint, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        content()
    }
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
    ) { Text(label) }
}

@Composable
private fun WeightSparkline(points: List<WeightPoint>) {
    val values = points.mapNotNull { it.weightKg }
    if (values.size < 2) {
        Text("记录再多一点，走势会更清楚。", color = Muted)
        return
    }
    val min = values.minOrNull() ?: return
    val max = values.maxOrNull() ?: return
    val span = (max - min).takeIf { it > 0.01 } ?: 1.0
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Paper.copy(alpha = 0.4f))
            .padding(12.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = size.width * index / (values.size - 1).coerceAtLeast(1)
                val y = size.height * (1f - ((value - min) / span).toFloat())
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = Accent,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            )
            values.forEachIndexed { index, value ->
                val x = size.width * index / (values.size - 1).coerceAtLeast(1)
                val y = size.height * (1f - ((value - min) / span).toFloat())
                drawCircle(color = Accent, radius = 5.dp.toPx(), center = Offset(x, y))
                drawCircle(color = Card, radius = 2.5.dp.toPx(), center = Offset(x, y))
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("${trimNum(min)} kg", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Text("${trimNum(max)} kg", color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun trimNum(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.1f", value)
