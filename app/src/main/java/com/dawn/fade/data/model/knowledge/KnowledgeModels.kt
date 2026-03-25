package com.dawn.fade.data.model.knowledge

/**
 * 体系数据模型，统一承载一级分类和二级分类结构。
 */
data class KnowledgeTreeItem(
    val children: List<KnowledgeChildItem> = emptyList(),
    val courseId: Int = 0,
    val id: Int = 0,
    val name: String = "",
    val order: Int = 0,
    val parentChapterId: Int = 0,
    val visible: Int = 1
)

data class KnowledgeChildItem(
    val children: List<KnowledgeChildItem> = emptyList(),
    val courseId: Int = 0,
    val id: Int = 0,
    val name: String = "",
    val order: Int = 0,
    val parentChapterId: Int = 0,
    val visible: Int = 1
)
