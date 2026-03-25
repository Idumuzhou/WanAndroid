package com.dawn.fade.data.model.home

/**
 * 首页 Banner 数据模型，统一承载轮播图展示字段。
 */
data class BannerItem(
    val desc: String? = null,
    val id: Int = 0,
    val imagePath: String? = null,
    val isVisible: Int = 0,
    val order: Int = 0,
    val title: String? = null,
    val type: Int = 0,
    val url: String? = null
) {
    val displayTitle: String
        get() = title.orEmpty().ifBlank { "首页推荐" }

    val safeUrl: String
        get() = url.orEmpty()

    val safeImagePath: String
        get() = imagePath.orEmpty()
}
