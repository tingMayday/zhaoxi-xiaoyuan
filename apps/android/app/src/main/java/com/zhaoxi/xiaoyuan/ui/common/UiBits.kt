package com.zhaoxi.xiaoyuan.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Ink
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.Paper
import java.time.LocalDate
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

/** 统一的「返回首页」顶栏：圆形返回钮 + 首页文案，下方可选眉题与标题。 */
@Composable
fun PageHeader(
    onBack: () -> Unit,
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    role = Role.Button,
                    onClick = onBack,
                )
                .background(Paper.copy(alpha = 0.55f), RoundedCornerShape(999.dp))
                .border(1.dp, Line, RoundedCornerShape(999.dp))
                .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Card)
                    .border(1.dp, Line, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回首页",
                    tint = Accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text("首页", color = Accent, style = MaterialTheme.typography.labelLarge)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(eyebrow, color = Accent, style = MaterialTheme.typography.labelLarge)
            Text(title, style = MaterialTheme.typography.headlineLarge)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, color = Muted)
            }
        }
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

@Composable
fun AppField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Accent,
            unfocusedBorderColor = Line,
            focusedLabelColor = Accent,
            unfocusedLabelColor = Muted,
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
            cursorColor = Accent,
            focusedContainerColor = Card,
            unfocusedContainerColor = Card,
        ),
        shape = RoundedCornerShape(14.dp),
    )
}

fun currentMonth(): String = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

fun todayDate(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

fun shiftMonth(month: String, delta: Long): String =
    YearMonth.parse(month).plusMonths(delta).format(DateTimeFormatter.ofPattern("yyyy-MM"))
