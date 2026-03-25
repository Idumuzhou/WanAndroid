package com.dawn.fade.main.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dawn.fade.data.model.base.ApiResult
import com.dawn.fade.data.repository.Repository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 首页 ViewModel，负责协调 Banner、文章流、刷新与分页加载。
 */
class HomeViewModel(
    private val repository: Repository = Repository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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

        _uiState.value = if (currentState.articles.isEmpty() && currentState.banners.isEmpty()) {
            currentState.copy(isInitialLoading = true, isRefreshing = false, isLoadingMore = false)
        } else {
            currentState.copy(isInitialLoading = false, isRefreshing = true, isLoadingMore = false)
        }

        viewModelScope.launch {
            val bannerDeferred = async { repository.fetchBannerList() }
            val articleDeferred = async { repository.fetchArticlePage(0) }

            val bannerResult = bannerDeferred.await()
            val articleResult = articleDeferred.await()

            val current = _uiState.value
            val banners = if (bannerResult is ApiResult.Success) {
                bannerResult.data
            } else {
                current.banners
            }

            val articles = if (articleResult is ApiResult.Success) {
                articleResult.data.datas
            } else {
                current.articles
            }

            _uiState.value = HomeUiState(
                banners = banners,
                articles = articles,
                isInitialLoading = false,
                isRefreshing = false,
                isLoadingMore = false,
                canLoadMore = articleResult is ApiResult.Success && !articleResult.data.over,
                nextPage = if (articleResult is ApiResult.Success) 1 else current.nextPage
            )

            if (bannerResult is ApiResult.Error && current.banners.isEmpty()) {
                _message.emit(bannerResult.message)
            }
            if (articleResult is ApiResult.Error) {
                _message.emit(articleResult.message)
            }
        }
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (!currentState.canLoadMore || currentState.isInitialLoading || currentState.isRefreshing || currentState.isLoadingMore) {
            return
        }

        _uiState.value = currentState.copy(isLoadingMore = true)

        viewModelScope.launch {
            when (val result = repository.fetchArticlePage(currentState.nextPage)) {
                is ApiResult.Success -> {
                    _uiState.value = currentState.copy(
                        articles = currentState.articles + result.data.datas,
                        isLoadingMore = false,
                        canLoadMore = !result.data.over,
                        nextPage = currentState.nextPage + 1
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = currentState.copy(isLoadingMore = false)
                    _message.emit(result.message)
                }
            }
        }
    }
}
