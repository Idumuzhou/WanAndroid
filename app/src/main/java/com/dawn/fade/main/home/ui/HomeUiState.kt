package com.dawn.fade.main.home.ui

import com.dawn.fade.data.model.article.ArticleItem
import com.dawn.fade.data.model.home.BannerItem

/**
 * 首页 UI 状态，统一维护 Banner、文章列表和分页加载状态。
 */
data class HomeUiState(
    val banners: List<BannerItem> = emptyList(),
    val articles: List<ArticleItem> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val nextPage: Int = 0
)
