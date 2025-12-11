package com.example.toutiaodemo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.toutiaodemo.data.db.NewsDatabase
import com.example.toutiaodemo.data.model.News
import com.example.toutiaodemo.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI状态类
data class NewsUiState(
    val newsList: List<News> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadMore: Boolean = false,
    val errorMsg: String? = null,
    val currentCategory: String = "top",
    val currentPage: Int = 1
)

class NewsViewModel(private val repository: NewsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    // 清空新闻列表
    fun clearNewsList() {
        _uiState.update { it.copy(newsList = emptyList()) }
    }

    // 切换分类
    fun switchCategory(category: String) {
        _uiState.update { it.copy(currentCategory = category) }
    }

    // 重置页码
    fun resetPage() {
        _uiState.update { it.copy(currentPage = 1) }
    }

    // 加载新闻
    fun loadNews(isLoadMore: Boolean) {
        val currentState = _uiState.value
        if (currentState.isLoading || (isLoadMore && currentState.isLoadMore)) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isLoadMore,
                    isLoadMore = isLoadMore,
                    errorMsg = null
                )
            }

            try {
                val page = if (isLoadMore) currentState.currentPage + 1 else 1

                val result = repository.getNewsList(
                    category = currentState.currentCategory,
                    page = page,
                    pageSize = 20
                )

                result.fold(
                    onSuccess = { newsList ->
                        val updatedList = if (isLoadMore) currentState.newsList + newsList else newsList
                        _uiState.update {
                            it.copy(
                                newsList = updatedList,
                                currentPage = page,
                                isLoading = false,
                                isLoadMore = false
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                errorMsg = e.message ?: "加载失败",
                                isLoading = false,
                                isLoadMore = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMsg = e.message ?: "加载异常",
                        isLoading = false,
                        isLoadMore = false
                    )
                }
            }
        }
    }

    // 工厂方法
    companion object {
        fun provideFactory(db: NewsDatabase): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = NewsRepository(db)
                    return NewsViewModel(repository) as T
                }
            }
        }
    }
}