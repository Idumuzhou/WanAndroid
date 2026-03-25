package com.dawn.fade.main.article.ui

import com.dawn.fade.data.model.article.ArticleItem

/**
 * 文章列表 UI 状态，统一维护加载、刷新、分页和列表数据。
 */
data class ArticleListUiState(
    val articles: List<ArticleItem> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val nextPage: Int = 0
)
