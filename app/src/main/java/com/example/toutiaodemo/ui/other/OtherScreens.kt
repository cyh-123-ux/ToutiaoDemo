// ui/other/OtherScreens.kt
package com.example.toutiaodemo.ui.other

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

// 视频页面（预留扩展接口）
@Composable
fun VideoScreen(
    onVideoFunctionClick: () -> Unit = {} // 预留视频功能点击接口
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "视频页面\n（功能待开发）",
            textAlign = TextAlign.Center
        )
        // 可通过onVideoFunctionClick扩展功能
        // Button(onClick = onVideoFunctionClick) { Text("加载视频") }
    }
}

// 搜索页面
@Composable
fun SearchScreen(
    onSearch: (String) -> Unit = {} // 预留搜索接口
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "搜索页面\n（功能待开发）",
            textAlign = TextAlign.Center
        )

    }
}