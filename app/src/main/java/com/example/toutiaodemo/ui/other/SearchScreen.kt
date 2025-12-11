package com.example.toutiaodemo.ui.other

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.toutiaodemo.data.db.NewsDatabase
import com.example.toutiaodemo.data.model.News
import com.example.toutiaodemo.data.repository.NewsRepository
import com.example.toutiaodemo.ui.components.NewsCard
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box

@Composable
fun SearchScreen(
    db: NewsDatabase, // 数据库实例（必传）
    onNewsClick: (News) -> Unit // 新闻点击回调（必传）
) {
    val coroutineScope = rememberCoroutineScope()
    val newsRepository = remember { NewsRepository(db) }
    var keyword by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<List<News>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // 内部封装搜索逻辑
    fun performSearch(keyword: String) {
        coroutineScope.launch {
            isLoading = true
            val result = newsRepository.searchNews(keyword)
            result.fold(
                onSuccess = { newsList ->
                    searchResult = newsList
                },
                onFailure = { /* 错误处理：可添加Toast提示 */ }
            )
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 搜索输入框
        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("输入新闻关键词搜索") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (keyword.isNotBlank()) {
                        performSearch(keyword)
                    }
                }
            ),
            singleLine = true
        )

        // 搜索按钮
        TextButton(
            onClick = {
                if (keyword.isNotBlank()) {
                    performSearch(keyword)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "搜索",
                color = Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )
        }

        // 搜索结果区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (keyword.isNotBlank() && searchResult.isEmpty()) {
                Text(
                    text = "未找到包含「$keyword」的新闻",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (searchResult.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResult) { news ->
                        NewsCard(news = news, onClick = { onNewsClick(news) })
                    }
                }
            }
        }
    }
}