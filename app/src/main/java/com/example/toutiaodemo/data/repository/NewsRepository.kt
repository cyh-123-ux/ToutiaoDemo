

package com.example.toutiaodemo.data.repository

import com.example.toutiaodemo.data.api.RetrofitClient
import com.example.toutiaodemo.data.db.NewsDatabase
import com.example.toutiaodemo.data.model.News
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import android.util.Log

class NewsRepository(private val db: NewsDatabase) {
    // 统一API密钥
//    private val apiKey = "91160f2028b1a2528b85a0ba82333980"//cyh
    //private val apiKey = "62a8706481909f79ee2c52ba52c7e845"
    private val apiKey = "598fff722fb64d22f93fd02503883dac"



    private val API_TIMEOUT = 10_000L // 10秒


    suspend fun getNewsList(
        category: String = "top",
        page: Int = 1,
        pageSize: Int = 20
    ): Result<List<News>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = withTimeout(API_TIMEOUT) {
                    RetrofitClient.instance.getNewsList(
                        category = category,
                        key = apiKey,
                        page = page,
                        pageSize = pageSize
                    )
                }


                Log.d("NewsRepository", "===== 聚合数据接口响应 =====")
                Log.d("NewsRepository", "分类：$category | 页码：$page | 页大小：$pageSize")
                Log.d("NewsRepository", "error_code：${response.code} | reason：${response.msg}")
                Log.d("NewsRepository", "stat：${response.data?.stat ?: "null"}")
                Log.d("NewsRepository", "新闻列表大小：${response.data?.newsList?.size ?: 0}")

                // 空值保护 + 聚合数据成功判断
                val networkNewsList = response.data?.newsList ?: emptyList()
                val isApiSuccess = response.code == 0 &&
                        response.msg == "success" &&
                        response.data?.stat == "1" &&
                        networkNewsList.isNotEmpty()

                if (networkNewsList.isNotEmpty()) {
                    // 过滤无效数据（id为空的新闻）
                    val validNews = networkNewsList.filter { it.id.isNotEmpty() }
                    if (validNews.isNotEmpty()) {
                        db.newsDao().insertNewsList(validNews)
                    }
                    Result.success(validNews)
                } else {
                    // API失败，读取本地缓存
                    val localNews = try {
                        db.newsDao().getNewsByCategory(category).first() ?: emptyList()
                    } catch (dbE: Exception) {
                        Log.e("NewsRepository", "读取本地缓存失败：${dbE.message}")
                        emptyList()
                    }
                    return@withContext if (localNews.isNotEmpty()) {
                        Result.success(localNews)
                    } else {
                        Result.failure(Throwable("暂无数据（接口返回：error_code=${response.code}，stat=${response.data?.stat}）"))
                    }
                }
            } catch (timeoutE: TimeoutCancellationException) {
                Log.e("NewsRepository", "请求超时（${API_TIMEOUT/1000}秒）：$category")
                val localNews = db.newsDao().getNewsByCategory(category).first() ?: emptyList()
                return@withContext if (localNews.isNotEmpty()) {
                    Result.success(localNews)
                } else {
                    Result.failure(Throwable("请求超时，请检查网络"))
                }
            } catch (e: Exception) {
                Log.e("NewsRepository", "接口异常：${e.message}", e)
                val localNews = try {
                    db.newsDao().getNewsByCategory(category).first() ?: emptyList()
                } catch (dbE: Exception) {
                    Log.e("NewsRepository", "本地缓存读取失败：${dbE.message}")
                    emptyList()
                }
                return@withContext if (localNews.isNotEmpty()) {
                    Result.success(localNews)
                } else {
                    Result.failure(Throwable("网络异常且本地无缓存：${e.message ?: "未知错误"}"))
                }
            }
        }
    }

    /**
     * 搜索功能
     */
    suspend fun searchNews(keyword: String): Result<List<News>> {
        return withContext(Dispatchers.IO) {
            if (keyword.isBlank()) {
                return@withContext Result.failure(Throwable("请输入搜索关键词"))
            }

            try {
                // 多分类拉取数据
                val categories = listOf("top", "yule", "keji", "tiyu")
                var apiNewsList = emptyList<News>()

                for (category in categories) {
                    val response = withTimeout(API_TIMEOUT) {
                        RetrofitClient.instance.getNewsList(
                            category = category,
                            key = apiKey,
                            page = 1,
                            pageSize = 20
                        )
                    }
                    // 聚合数据成功判断
                    if (response.code == 0 && response.msg == "success" && response.data?.stat == "1") {
                        apiNewsList += response.data?.newsList ?: emptyList()
                    }
                }

                // 精准过滤
                val filteredApiNews = apiNewsList.filter {
                    it.title.contains(keyword, ignoreCase = true) ||
                            it.source.contains(keyword, ignoreCase = true) ||
                            it.isContent == "1" // 仅过滤有内容的新闻
                }

                // 本地搜索
                val localAllNews = try {
                    db.newsDao().getAllNews().first() ?: emptyList()
                } catch (dbE: Exception) {
                    Log.e("NewsRepository", "本地搜索失败：${dbE.message}")
                    emptyList()
                }
                val filteredLocalNews = localAllNews.filter {
                    it.title.contains(keyword, ignoreCase = true) ||
                            it.source.contains(keyword, ignoreCase = true)
                }

                // 合并去重
                val mergedNews = (filteredApiNews + filteredLocalNews)
                    .distinctBy { it.id }
                    .take(30)

                Log.d("NewsRepository", "搜索[$keyword]：API${filteredApiNews.size}条 | 本地${filteredLocalNews.size}条 | 合并${mergedNews.size}条")
                Result.success(mergedNews)
            } catch (timeoutE: TimeoutCancellationException) {
                Result.failure(Throwable("搜索超时，请稍后重试"))
            } catch (e: Exception) {
                Log.e("NewsRepository", "搜索异常：${e.message}", e)
                Result.failure(Throwable("搜索失败：${e.message ?: "未知错误"}"))
            }
        }
    }


    // 监听本地新闻
    fun observeNewsByCategory(category: String): Flow<List<News>> {
        return db.newsDao().getNewsByCategory(category)
    }

    // 清空新闻
    suspend fun clearNews(category: String) {
        withContext(Dispatchers.IO) {
            try {
                val deleteCount = db.newsDao().clearNewsByCategory(category)
                Log.d("NewsRepository", "清空[$category]分类新闻：删除${deleteCount}条")
            } catch (e: Exception) {
                Log.e("NewsRepository", "清空新闻失败：${e.message}", e)
            }
        }
    }

    // 读取所有本地新闻
    suspend fun getAllLocalNews(): List<News> {
        return withContext(Dispatchers.IO) {
            try {
                db.newsDao().getAllNews().first() ?: emptyList()
            } catch (e: Exception) {
                Log.e("NewsRepository", "读取所有本地新闻失败：${e.message}")
                emptyList()
            }
        }
    }
}