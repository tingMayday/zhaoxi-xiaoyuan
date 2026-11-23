package com.zhaoxi.xiaoyuan.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Ink
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.Paper

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val isRegister = state.mode == LoginUiState.Mode.Register

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(horizontal = 28.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("朝夕小院", style = MaterialTheme.typography.labelLarge, color = Muted)
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isRegister) "为自己留一间院子" else "回来就好",
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(8.dp))
        Text("记收支、习惯、身体、日程、待买和收藏。", color = Muted)

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Card, RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isRegister) {
                Field(label = "怎么称呼", value = state.name, onChange = viewModel::setName)
            }
            Field(
                label = "邮箱",
                value = state.email,
                onChange = viewModel::setEmail,
                keyboardType = KeyboardType.Email,
            )
            Field(
                label = "密码",
                value = state.password,
                onChange = viewModel::setPassword,
                keyboardType = KeyboardType.Password,
                password = true,
            )

            state.error?.let {
                Text(it, color = Accent, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = { viewModel.submit(onLoggedIn) },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = OnAccent,
                    )
                } else {
                    Text(if (isRegister) "建立小院" else "进入小院")
                }
            }

            TextButton(
                onClick = viewModel::toggleMode,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = if (isRegister) "已有账号，登录" else "第一次来，注册",
                    color = Accent,
                )
            }
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
