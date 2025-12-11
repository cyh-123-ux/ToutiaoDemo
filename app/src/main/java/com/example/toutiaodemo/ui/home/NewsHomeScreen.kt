package com.example.toutiaodemo.ui.home


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toutiaodemo.data.db.NewsDatabase
import com.example.toutiaodemo.data.model.News
import com.example.toutiaodemo.ui.components.NewsCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ========== 分类标签 ==========
private val categories = listOf("推荐", "本地", "财经", "娱乐")

// ========== 首页主布局 ==========
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NewsHomeScreen(
    db: NewsDatabase,
    onNewsClick: (News) -> Unit,
    viewModel: NewsViewModel = viewModel(
        factory = NewsViewModel.provideFactory(db)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 分类切换时清空旧数据+加载新数据
    LaunchedEffect(pagerState.currentPage) {
        val targetCategory = when (categories[pagerState.currentPage]) {
            "推荐" -> "top"
            "本地" -> "guonei"
            "财经" -> "caijing"
            "娱乐" -> "yule"
            else -> "top"
        }
        // 先切换分类+重置页码+清空列表，再加载
        viewModel.switchCategory(targetCategory)
        viewModel.resetPage()
        viewModel.clearNewsList()
        viewModel.loadNews(isLoadMore = false)
    }

    // 加载更多逻辑
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1
        }.collectLatest { isAtBottom ->
            if (isAtBottom && !uiState.isLoading && !uiState.isLoadMore && uiState.newsList.isNotEmpty()) {
                delay(300)
                viewModel.loadNews(isLoadMore = true)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("今日头条", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 分类标签栏
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                categories.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // 新闻列表分页器
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { _ ->
                NewsListContent(
                    newsList = uiState.newsList,
                    isLoading = uiState.isLoading,
                    isLoadMore = uiState.isLoadMore,
                    errorMsg = uiState.errorMsg,
                    listState = listState,
                    onNewsClick = onNewsClick
                )
            }
        }
    }
}

// ========== 新闻列表内容 ==========
@Composable
fun NewsListContent(
    newsList: List<News>,
    isLoading: Boolean,
    isLoadMore: Boolean,
    errorMsg: String?,
    listState: LazyListState,
    onNewsClick: (News) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
        ) {
            items(newsList, key = { it.id }) { news ->
                NewsCard(news = news, onClick = { onNewsClick(news) })
            }

            if (isLoadMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // 加载中/错误/空数据提示
        if (isLoading && newsList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (!isLoading && errorMsg != null && newsList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = errorMsg,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }

        if (!isLoading && errorMsg == null && newsList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "暂无相关新闻",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}