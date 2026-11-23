package com.zhaoxi.xiaoyuan.ui.placeholder

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted

@Composable
fun PlaceholderScreen(title: String, note: String = "这一页会在下一期补上。") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Line, RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$title\n$note",
                color = Muted,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
