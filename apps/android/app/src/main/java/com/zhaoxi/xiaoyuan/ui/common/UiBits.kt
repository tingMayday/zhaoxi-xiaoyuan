package com.zhaoxi.xiaoyuan.ui.common

import android.widget.NumberPicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Ink
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Paper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun EmptyBox(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Muted)
    }
}

/** 子页标题区：庭院纹理 + 眉题/标题，不含返回钮（返回用悬浮钮）。 */
@Composable
fun PageHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Paper.copy(alpha = 0.45f)),
    ) {
        SoftYardPattern(
            modifier = Modifier
                .matchParentSize()
                .padding(4.dp),
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(eyebrow, color = Accent, style = MaterialTheme.typography.labelLarge)
            Text(title, style = MaterialTheme.typography.headlineLarge)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, color = Muted)
            }
        }
    }
}

/** 悬浮返回首页：叠在内容之上，不占文档流高度。 */
@Composable
fun FloatingHomeButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var navigating by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Card.copy(alpha = 0.92f))
            .border(1.dp, Line, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                role = Role.Button,
                onClick = {
                    if (!navigating) {
                        navigating = true
                        onBack()
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "返回首页",
            tint = Accent,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确定",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText, color = Accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = Muted) }
        },
        containerColor = Card,
    )
}

enum class FieldKind {
    Text,
    Decimal,
    Number,
    Date,
    DateTime,
}

private val fieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Accent,
        unfocusedBorderColor = Line,
        focusedLabelColor = Accent,
        unfocusedLabelColor = Muted,
        focusedTextColor = Ink,
        unfocusedTextColor = Ink,
        disabledTextColor = Ink,
        disabledBorderColor = Line,
        disabledLabelColor = Muted,
        disabledContainerColor = Card,
        cursorColor = Accent,
        focusedContainerColor = Card,
        unfocusedContainerColor = Card,
    )

@Composable
fun AppField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    kind: FieldKind = FieldKind.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    when (kind) {
        FieldKind.Date -> DateAppField(label, value, onChange, modifier)
        FieldKind.DateTime -> DateTimeAppField(label, value, onChange, modifier)
        else -> {
            val keyboardType = when (kind) {
                FieldKind.Decimal -> KeyboardType.Decimal
                FieldKind.Number -> KeyboardType.Number
                else -> KeyboardType.Text
            }
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                modifier = modifier.fillMaxWidth(),
                label = { Text(label) },
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
            )
        }
    }
}

@Composable
private fun DateAppField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val initial = runCatching { LocalDate.parse(value) }.getOrElse { LocalDate.now() }

    PickerFieldShell(label = label, value = value, modifier = modifier) { open = true }

    if (open) {
        WheelDateTimeDialog(
            title = label,
            initial = initial.atTime(0, 0),
            withTime = false,
            onDismiss = { open = false },
            onConfirm = { picked ->
                onChange(picked.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                open = false
            },
        )
    }
}

@Composable
private fun DateTimeAppField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val initial = runCatching {
        LocalDateTime.parse(value.replace(' ', 'T'))
    }.getOrElse {
        runCatching { LocalDate.parse(value.take(10)).atTime(9, 0) }.getOrElse { LocalDateTime.now() }
    }

    PickerFieldShell(label = label, value = value, modifier = modifier) { open = true }

    if (open) {
        WheelDateTimeDialog(
            title = label,
            initial = initial,
            withTime = true,
            onDismiss = { open = false },
            onConfirm = { picked ->
                onChange(picked.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                open = false
            },
        )
    }
}

@Composable
private fun PickerFieldShell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            singleLine = true,
            readOnly = true,
            enabled = false,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onOpen),
        )
    }
}

/** 滚轮一次选完日期（可选时间），点确定即可。 */
@Composable
private fun WheelDateTimeDialog(
    title: String,
    initial: LocalDateTime,
    withTime: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit,
) {
    var year by remember { mutableIntStateOf(initial.year) }
    var month by remember { mutableIntStateOf(initial.monthValue) }
    var day by remember { mutableIntStateOf(initial.dayOfMonth) }
    var hour by remember { mutableIntStateOf(initial.hour) }
    var minute by remember { mutableIntStateOf(initial.minute) }
    val maxDay = remember(year, month) { YearMonth.of(year, month).lengthOfMonth() }
    LaunchedEffect(maxDay) {
        if (day > maxDay) day = maxDay
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("年 / 月 / 日", color = Muted, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WheelPicker(value = year, range = 2020..2035, onChange = { year = it })
                    WheelPicker(value = month, range = 1..12, onChange = { month = it })
                    WheelPicker(value = day, range = 1..maxDay, onChange = { day = it })
                }
                if (withTime) {
                    Text("时 / 分", color = Muted, style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WheelPicker(value = hour, range = 0..23, onChange = { hour = it })
                        Text(":", style = MaterialTheme.typography.titleLarge)
                        WheelPicker(value = minute, range = 0..59, onChange = { minute = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val safeDay = day.coerceIn(1, YearMonth.of(year, month).lengthOfMonth())
                    onConfirm(LocalDateTime.of(year, month, safeDay, hour, minute))
                },
            ) { Text("确定", color = Accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = Muted) }
        },
        containerColor = Card,
    )
}

@Composable
private fun WheelPicker(value: Int, range: IntRange, onChange: (Int) -> Unit) {
    AndroidView(
        modifier = Modifier.height(120.dp),
        factory = { context ->
            NumberPicker(context).apply {
                minValue = range.first
                maxValue = range.last
                this.value = value.coerceIn(range.first, range.last)
                wrapSelectorWheel = true
                setOnValueChangedListener { _, _, newVal -> onChange(newVal) }
            }
        },
        update = { picker ->
            if (picker.minValue != range.first || picker.maxValue != range.last) {
                picker.minValue = range.first
                picker.maxValue = range.last
            }
            val next = value.coerceIn(range.first, range.last)
            if (picker.value != next) picker.value = next
        },
    )
}

enum class ActionTone {
    Primary,
    Danger,
    Soft,
}

/** 完成 / 删除等操作按钮，视觉更醒目。 */
@Composable
fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ActionTone = ActionTone.Primary,
    enabled: Boolean = true,
) {
    val bg = when (tone) {
        ActionTone.Primary -> Accent
        ActionTone.Danger -> Color(0xFF8A4550)
        ActionTone.Soft -> Card
    }
    val fg = when (tone) {
        ActionTone.Primary, ActionTone.Danger -> OnAccent
        ActionTone.Soft -> Accent
    }
    val border = when (tone) {
        ActionTone.Soft -> Line
        else -> bg
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) bg else bg.copy(alpha = 0.4f))
            .border(1.dp, if (enabled) border else border.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (enabled) fg else fg.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/** 柔和庭院纹理：弧顶与点阵，丰富页面层次。 */
@Composable
fun SoftYardPattern(
    modifier: Modifier = Modifier,
    accent: Color = Accent.copy(alpha = 0.14f),
    ink: Color = Ink.copy(alpha = 0.06f),
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val roof = Path().apply {
            moveTo(w * 0.12f, h * 0.42f)
            quadraticTo(w * 0.5f, h * 0.08f, w * 0.88f, h * 0.42f)
        }
        drawPath(roof, color = accent, style = Stroke(width = 6.dp.toPx()))
        drawCircle(color = accent, radius = 18.dp.toPx(), center = Offset(w * 0.5f, h * 0.58f))
        val step = 22.dp.toPx()
        var y = step
        while (y < h) {
            var x = step * 0.5f
            while (x < w) {
                drawCircle(color = ink, radius = 1.6.dp.toPx(), center = Offset(x, y))
                x += step
            }
            y += step
        }
    }
}

/** 金额等数字：单行自适应缩小字号完整显示；默认不可点，不弹框。 */
@Composable
fun MoneyValue(
    value: String,
    modifier: Modifier = Modifier,
    color: Color = Ink,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    label: String? = null,
    clickable: Boolean = false,
) {
    var showFull by remember { mutableStateOf(false) }
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val maxSp = style.fontSize.takeIf { it != TextUnit.Unspecified } ?: 16.sp
    val minSp = 10.sp

    BoxWithConstraints(
        modifier = modifier
            .height(with(density) { maxSp.toDp() * 1.35f })
            .then(if (clickable) Modifier.clickable { showFull = true } else Modifier),
        contentAlignment = Alignment.CenterStart,
    ) {
        val maxWidthPx = constraints.maxWidth
        val fitted = remember(value, maxWidthPx, maxSp, style, color) {
            var low = minSp.value
            var high = maxSp.value
            var best = minSp
            repeat(8) {
                val mid = ((low + high) / 2f)
                val midSp = mid.sp
                val width = measurer.measure(
                    text = value,
                    style = style.copy(fontSize = midSp, color = color),
                    maxLines = 1,
                    softWrap = false,
                ).size.width
                if (width <= maxWidthPx || maxWidthPx <= 0) {
                    best = midSp
                    low = mid
                } else {
                    high = mid
                }
            }
            best
        }
        Text(
            text = value,
            color = color,
            style = style.copy(fontSize = fitted),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }

    if (clickable && showFull) {
        AlertDialog(
            onDismissRequest = { showFull = false },
            title = { Text(label ?: "完整数值") },
            text = { Text(value, style = MaterialTheme.typography.titleLarge) },
            confirmButton = {
                TextButton(onClick = { showFull = false }) { Text("好的", color = Accent) }
            },
            containerColor = Card,
        )
    }
}

@Composable
fun ToastMessage(message: String?, onConsumed: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onConsumed()
        }
    }
}

fun currentMonth(): String = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

fun todayDate(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

fun shiftMonth(month: String, delta: Long): String =
    YearMonth.parse(month).plusMonths(delta).format(DateTimeFormatter.ofPattern("yyyy-MM"))
