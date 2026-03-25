package com.dawn.fade.main.dynamic.ui

import com.dawn.fade.data.model.knowledge.KnowledgeTreeItem

/**
 * 体系页 UI 状态，统一维护知识体系数据和加载状态。
 */
data class KnowledgeUiState(
    val knowledgeTree: List<KnowledgeTreeItem> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false
)
