package com.dawn.fade.main.dynamic.ui

import com.dawn.fade.data.model.article.ArticleItem

/**
 * 分类文章页和作者搜索页共用的文章列表状态。
 */
data class KnowledgeArticleListUiState(
    val articles: List<ArticleItem> = emptyList(),
    val hasRequested: Boolean = false,
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val nextPage: Int = 0
)
