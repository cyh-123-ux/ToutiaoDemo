
package com.example.toutiaodemo.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

@Composable
fun LoginRegisterScreen(
    onLoginSuccess: () -> Unit
) {
    // 使用全局单例的ViewModel
    val authViewModel = AuthSingleton.INSTANCE
    val authState by authViewModel.authState.collectAsStateWithLifecycle(initialValue = AuthUiState())

    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("登录", "注册")

    var loginPhone by remember { mutableStateOf("") }
    var loginPwd by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPwd by remember { mutableStateOf("") }

    // 监听登录状态变化
    LaunchedEffect(authState.currentUser.isLogin) {
        if (authState.currentUser.isLogin) {
            println("登录状态已更新：${authState.currentUser}")
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "今日头条",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(30.dp)
        )

        TabRow(
            selectedTabIndex = tabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(30.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (tabIndex == 1) {
                OutlinedTextField(
                    value = regUsername,
                    onValueChange = { regUsername = it },
                    label = { Text("用户名") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !authState.isLoading
                )
            }

            OutlinedTextField(
                value = if (tabIndex == 0) loginPhone else regPhone,
                onValueChange = { if (tabIndex == 0) loginPhone = it else regPhone = it },
                label = { Text("手机号") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !authState.isLoading
            )

            OutlinedTextField(
                value = if (tabIndex == 0) loginPwd else regPwd,
                onValueChange = { if (tabIndex == 0) loginPwd = it else regPwd = it },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !authState.isLoading
            )

            if (authState.errorMsg != null) {
                Text(
                    text = authState.errorMsg!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Button(
                onClick = {
                    if (tabIndex == 0) {
                        authViewModel.login(loginPhone, loginPwd)
                    } else {
                        authViewModel.register(regUsername, regPhone, regPwd)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authState.isLoading
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = if (tabIndex == 0) "登录" else "注册")
                }
            }
        }
    }
}