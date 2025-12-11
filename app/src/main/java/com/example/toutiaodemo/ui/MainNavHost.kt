package com.example.toutiaodemo.ui


// 图标核心包
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
// Material3核心组件
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
// 布局修饰符
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
// 状态与生命周期
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// 基础类型
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
// 导航组件
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// 项目内部依赖
import com.example.toutiaodemo.data.db.NewsDatabase
import com.example.toutiaodemo.data.model.News
import com.example.toutiaodemo.data.repository.NewsRepository
import com.example.toutiaodemo.ui.detail.NewsDetailScreen
import com.example.toutiaodemo.ui.home.NewsHomeScreen
import com.example.toutiaodemo.ui.other.SearchScreen
import com.example.toutiaodemo.ui.profile.AuthSingleton
import com.example.toutiaodemo.ui.profile.AuthUiState
import com.example.toutiaodemo.ui.profile.LoginRegisterScreen
import com.example.toutiaodemo.ui.profile.ProfileScreen
// 协程相关
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import android.util.Log

// 路由定义
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Profile : Screen("profile")

    object NewsDetail : Screen("news_detail/{newsId}/{category}") {

        fun createRoute(news: News) = "news_detail/${news.id}/${news.category}"
    }
    object Login : Screen("login")
}

// 底部导航项模型
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// 包装原有首页
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedNewsHomeScreen(
    db: NewsDatabase,
    onNewsClick: (News) -> Unit
) {
    NewsHomeScreen(
        db = db,
        onNewsClick = onNewsClick
    )
}

// 主导航
@Composable
fun MainNavHost(
    db: NewsDatabase,
    navController: NavHostController = rememberNavController()
) {
    val authViewModel = AuthSingleton.INSTANCE
    val authState by authViewModel.authState.collectAsStateWithLifecycle(
        initialValue = AuthUiState()
    )

    // 登录拦截逻辑
    LaunchedEffect(authState.currentUser.isLogin) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (!authState.currentUser.isLogin && currentRoute != Screen.Login.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authState.currentUser.isLogin) Screen.Home.route else Screen.Login.route
    ) {
        // 登录页
        composable(Screen.Login.route) {
            LoginRegisterScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 首页
        composable(Screen.Home.route) {
            WrappedNewsHomeScreen(
                db = db,
                onNewsClick = { news ->
                    // 关键修复3：跳转详情页时携带分类参数
                    navController.navigate(Screen.NewsDetail.createRoute(news))
                }
            )
        }

        // 搜索页
        composable(Screen.Search.route) {
            val coroutineScope = rememberCoroutineScope()
            var searchResult by remember { mutableStateOf<List<News>>(emptyList()) }
            var isLoading by remember { mutableStateOf(false) }

            SearchScreen(
                db = db,
                onNewsClick = { news: News ->
                    // 关键修复4：搜索页跳转详情页也携带分类
                    navController.navigate(Screen.NewsDetail.createRoute(news))
                }
            )
        }

        // 我的页面
        composable(Screen.Profile.route) {
            ProfileScreen(
                onGoLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        // 新闻详情页
        composable(Screen.NewsDetail.route) { backStackEntry ->
            // 关键修复5：获取newsId和category参数
            val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
            val category = backStackEntry.arguments?.getString("category") ?: "top" // 兜底top
            val coroutineScope = rememberCoroutineScope()
            val newsRepository = remember { NewsRepository(db) }

            // 用mutableStateOf管理新闻数据
            val (news, setNews) = remember { mutableStateOf<News?>(null) }
            val (isLoading, setIsLoading) = remember { mutableStateOf(true) }

            // 加载真实数据
            LaunchedEffect(newsId, category) { // 依赖category，分类变化重新加载
                setIsLoading(true)
                try {
                    // 切换到IO线程执行数据库/网络操作
                    withContext(Dispatchers.IO) {
                        // 1. 先查本地数据库
                        val localNewsList = newsRepository.observeNewsByCategory(category).first()
                        val clickedNews = localNewsList.find { it.id == newsId }
                        Log.d("DetailLoad", "本地查询：分类=$category，找到新闻=${clickedNews != null}")

                        if (clickedNews != null) {
                            setNews(clickedNews)
                        } else {
                            // 2. 本地无缓存，请求接口
                            val apiResult = newsRepository.getNewsList(
                                category = category,
                                page = 1,
                                pageSize = 20
                            )
                            if (apiResult.isSuccess) {
                                val apiNewsList = apiResult.getOrNull() ?: emptyList()
                                val apiClickedNews = apiNewsList.find { it.id == newsId }
                                Log.d("DetailLoad", "接口查询：分类=$category，找到新闻=${apiClickedNews != null}")
                                // 兜底新闻（使用动态category）
                                setNews(apiClickedNews ?: News(
                                    id = newsId,
                                    title = "暂无标题",
                                    newsUrl = "",
                                    source = "未知来源",
                                    publishTime = "",
                                    category = category,
                                    imageUrl = "",
                                    isContent = "0"
                                ))
                            } else {
                                // 接口失败日志
                                val errorMsg = apiResult.exceptionOrNull()?.message ?: "接口请求失败"
                                Log.e("DetailLoad", "接口加载失败：$errorMsg")
                                setNews(News(
                                    id = newsId,
                                    title = "暂无标题",
                                    newsUrl = "",
                                    source = "未知来源",
                                    publishTime = "",
                                    category = category,
                                    imageUrl = "",
                                    isContent = "0"
                                ))
                            }
                        }
                    }
                } catch (e: Exception) {
                    // 异常兜底+日志
                    Log.e("DetailLoad", "加载新闻异常：${e.message}", e)
                    setNews(News(
                        id = newsId,
                        title = "暂无标题",
                        newsUrl = "",
                        source = "未知来源",
                        publishTime = "",
                        category = category,
                        imageUrl = "",
                        isContent = "0"
                    ))
                } finally {
                    setIsLoading(false)
                }
            }

            // 显示不同状态
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "加载详情中...")
                    }
                }
                news != null -> {
                    NewsDetailScreen(
                        news = news,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "暂无该新闻详情")
                    }
                }
            }
        }
    }
}

// 底部导航主界面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(db: NewsDatabase) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem(
            label = "首页",
            icon = Icons.Filled.Home,
            route = Screen.Home.route
        ),
        BottomNavItem(
            label = "搜索",
            icon = Icons.Filled.Search,
            route = Screen.Search.route
        ),
        BottomNavItem(
            label = "我的",
            icon = Icons.Filled.Person,
            route = Screen.Profile.route
        )
    )

    Scaffold(
        bottomBar = {
            BottomAppBar {
                bottomNavItems.forEach { item ->
                    val selected = navController.currentBackStackEntry?.destination?.route == item.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MainNavHost(db = db, navController = navController)
        }
    }
}