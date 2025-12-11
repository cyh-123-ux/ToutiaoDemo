package com.example.toutiaodemo.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage

@Composable
fun ProfileScreen(
    onGoLogin: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    // 使用和登录页同一个全局单例
    val authViewModel = AuthSingleton.INSTANCE
    val authState by authViewModel.authState.collectAsStateWithLifecycle(initialValue = AuthUiState())
    val currentUser = authState.currentUser

    // 打印状态日志
    LaunchedEffect(currentUser) {
        println("我的页面当前用户状态：$currentUser")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (currentUser.isLogin) {
            // 已登录UI
            SubcomposeAsyncImage(
                model = currentUser.avatar,
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .padding(top = 40.dp, bottom = 16.dp),
                loading = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            Text(
                text = currentUser.username,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = currentUser.phone,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(8.dp)
            )

            Column(
                modifier = Modifier.padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "我的收藏：${currentUser.collectCount} 篇",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "已读新闻：${currentUser.readCount} 篇",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    authViewModel.logout()
                    onLogoutSuccess()
                },
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text("退出登录")
            }
        } else {
            // 未登录UI
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "请先登录",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = onGoLogin,
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text("立即登录/注册")
                }
            }
        }
    }
}