package com.dawn.fade.main.dynamic.ui

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
 * 分类文章页和作者搜索页共用的分页 ViewModel。
 */
class KnowledgeArticleListViewModel(
    private val repository: Repository = Repository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(KnowledgeArticleListUiState())
    val uiState: StateFlow<KnowledgeArticleListUiState> = _uiState.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message.asSharedFlow()

    private var currentCid: Int? = null
    private var currentAuthor: String? = null
    private var initialized = false

    fun setupCategory(cid: Int) {
        if (initialized && currentCid == cid && currentAuthor == null) return
        currentCid = cid
        currentAuthor = null
        initialized = true
        refresh()
    }

    fun searchByAuthor(author: String) {
        val keyword = author.trim()
        if (keyword.isBlank()) {
            return
        }
        currentCid = null
        currentAuthor = keyword
        initialized = true
        refresh()
    }

    fun refresh() {
        val currentState = _uiState.value
        if (!initialized || currentState.isRefreshing || currentState.isLoadingMore) {
            return
        }

        _uiState.value = if (currentState.articles.isEmpty()) {
            currentState.copy(
                hasRequested = true,
                isInitialLoading = true,
                isRefreshing = false,
                isLoadingMore = false
            )
        } else {
            currentState.copy(
                hasRequested = true,
                isInitialLoading = false,
                isRefreshing = true,
                isLoadingMore = false
            )
        }

        loadPage(page = 0, append = false)
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (!initialized || !currentState.canLoadMore || currentState.isInitialLoading || currentState.isRefreshing || currentState.isLoadingMore) {
            return
        }

        _uiState.value = currentState.copy(isLoadingMore = true)
        loadPage(page = currentState.nextPage, append = true)
    }

    private fun loadPage(page: Int, append: Boolean) {
        viewModelScope.launch {
            when (
                val result = repository.fetchArticlePage(
                    page = page,
                    cid = currentCid,
                    author = currentAuthor
                )
            ) {
                is ApiResult.Success -> {
                    val pageData = result.data
                    val mergedArticles = if (append) {
                        _uiState.value.articles + pageData.datas
                    } else {
                        pageData.datas
                    }

                    _uiState.value = KnowledgeArticleListUiState(
                        articles = mergedArticles,
                        hasRequested = true,
                        isInitialLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        canLoadMore = !pageData.over,
                        nextPage = page + 1
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        hasRequested = true,
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
