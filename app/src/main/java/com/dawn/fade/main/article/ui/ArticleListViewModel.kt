package com.dawn.fade.main.article.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dawn.fade.data.model.base.ApiResult
import com.dawn.fade.data.repository.Repository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 文章列表 ViewModel，负责协调刷新、分页和错误消息分发。
 */
class ArticleListViewModel(
    private val repository: Repository = Repository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArticleListUiState())
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        val currentState = _uiState.value
        if (currentState.isRefreshing || currentState.isLoadingMore) {
            return
        }

        _uiState.value = if (currentState.articles.isEmpty()) {
            currentState.copy(isInitialLoading = true, isRefreshing = false, isLoadingMore = false)
        } else {
            currentState.copy(isInitialLoading = false, isRefreshing = true, isLoadingMore = false)
        }

        loadPage(page = 0, append = false)
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (!currentState.canLoadMore || currentState.isInitialLoading || currentState.isRefreshing || currentState.isLoadingMore) {
            return
        }

        _uiState.value = currentState.copy(isLoadingMore = true)
        loadPage(page = currentState.nextPage, append = true)
    }

    private fun loadPage(page: Int, append: Boolean) {
        viewModelScope.launch {
            when (val result = repository.fetchArticlePage(page)) {
                is ApiResult.Success -> {
                    val pageData = result.data
                    val mergedArticles = if (append) {
                        _uiState.value.articles + pageData.datas
                    } else {
                        pageData.datas
                    }

                    _uiState.value = ArticleListUiState(
                        articles = mergedArticles,
                        isInitialLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        canLoadMore = !pageData.over,
                        nextPage = page + 1
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false
                    )
                    _message.emit(result.message)
                }
            }
        }
    }
}
