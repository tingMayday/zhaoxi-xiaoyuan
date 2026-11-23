package com.zhaoxi.xiaoyuan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ZhaoXiColors = lightColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    secondary = Clay,
    onSecondary = OnAccent,
    background = Card,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = Muted,
    outline = Line,
)

@Composable
fun ZhaoXiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ZhaoXiColors,
        typography = ZhaoXiTypography,
        content = content,
    )
}
