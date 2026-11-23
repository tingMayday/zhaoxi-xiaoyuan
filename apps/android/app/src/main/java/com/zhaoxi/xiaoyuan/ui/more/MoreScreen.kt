package com.zhaoxi.xiaoyuan.ui.more

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zhaoxi.xiaoyuan.data.auth.AuthRepository
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import kotlinx.coroutines.launch

@Composable
fun MoreScreen(
    authRepository: AuthRepository,
    onOpenSchedule: () -> Unit,
    onOpenBuy: () -> Unit,
    onOpenMedia: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenSettings: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("更多", style = MaterialTheme.typography.headlineMedium)
        Text("日程、待买、收藏、档案和院主资料。", color = Muted, style = MaterialTheme.typography.bodyLarge)

        MoreLink("日程", "安排今天要做的事", onOpenSchedule)
        MoreLink("待买", "想到就记下", onOpenBuy)
        MoreLink("书影音", "把看过的收进收藏", onOpenMedia)
        MoreLink("时光档案", "所有日常，都有出处", onOpenArchive)
        MoreLink("设置", "称呼、时区与身高", onOpenSettings)

        Button(
            onClick = {
                scope.launch {
                    authRepository.logout()
                    onLoggedOut()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
        ) {
            Text("离开")
        }
    }
}

@Composable
private fun MoreLink(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text("进入", color = Accent)
        }
        Text(subtitle, color = Muted)
    }
}
