package com.example.toutiaodemo.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import android.webkit.WebSettings
import android.webkit.WebView
import com.example.toutiaodemo.data.model.News

/**
 * 新闻详情页（完全匹配真实JSON字段）
 * - news.url → 新闻详情网页链接（核心）
 * - news.thumbnail_pic_s/s02/s03 → 新闻图片
 * - news.author_name → 来源
 * - news.date → 发布时间
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(news: News, onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新闻详情", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. 新闻标题
            Text(
                text = news.title.ifEmpty { "暂无标题" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // 2. 来源+发布时间
            Text(
                text = "${news.source.ifEmpty { "未知来源" }} · ${news.publishTime.ifEmpty { "未知时间" }}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 0.dp)
                    .padding(bottom = 16.dp),


            )

            // 3. 新闻主图
            val displayImageUrl = when {
                !news.imageUrl.isNullOrEmpty() -> news.imageUrl // thumbnail_pic_s
                !news.imageUrl2.isNullOrEmpty() -> news.imageUrl2 // thumbnail_pic_s02
                !news.imageUrl3.isNullOrEmpty() -> news.imageUrl3 // thumbnail_pic_s03
                else -> null
            }
            if (displayImageUrl != null) {
                AsyncImage(
                    model = displayImageUrl,
                    contentDescription = news.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp, vertical = 0.dp)
                        .padding(bottom = 16.dp),
                    // 图片加载失败兜底
                    error = coil.compose.rememberAsyncImagePainter(model = "https://via.placeholder.com/400x200?text=暂无图片")
                )
            }

            // 4. 新闻详情正文
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (news.newsUrl.isNotEmpty()) { // newsUrl 是映射后的 url 字段
                    // WebView加载详情链接（直接显示新闻正文）
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                // 启用JS，避免网页内容显示不全
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                // 加载JSON的url字段对应的网页
                                loadUrl(news.newsUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // 无详情链接时的兜底提示
                    Text(
                        text = "暂无新闻详情链接",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

// 补充滚动状态的导入（避免Unresolved reference）
@Composable
fun rememberScrollState(initialValue: Int = 0) = androidx.compose.foundation.rememberScrollState(initialValue)