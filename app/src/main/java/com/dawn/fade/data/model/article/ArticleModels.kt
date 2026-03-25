package com.dawn.fade.data.model.article

import androidx.core.text.HtmlCompat

/**
 * 文章接口数据模型，集中维护列表响应与文章项结构。
 */
data class ArticlePageData(
    val curPage: Int,
    val datas: List<ArticleItem>,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int
)

data class ArticleItem(
    val adminAdd: Boolean,
    val apkLink: String?,
    val audit: Int,
    val author: String?,
    val canEdit: Boolean,
    val chapterId: Int,
    val chapterName: String?,
    val collect: Boolean,
    val courseId: Int,
    val desc: String?,
    val descMd: String?,
    val envelopePic: String?,
    val fresh: Boolean,
    val host: String?,
    val id: Int,
    val isAdminAdd: Boolean,
    val link: String?,
    val niceDate: String?,
    val niceShareDate: String?,
    val origin: String?,
    val prefix: String?,
    val projectLink: String?,
    val publishTime: Long,
    val realSuperChapterId: Int,
    val selfVisible: Int,
    val shareDate: Long,
    val shareUser: String?,
    val superChapterId: Int,
    val superChapterName: String?,
    val tags: List<ArticleTag>?,
    val title: String?,
    val type: Int,
    val userId: Int,
    val visible: Int,
    val zan: Int
) {
    val displayTitle: String
        get() = HtmlCompat.fromHtml(title.orEmpty(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

    val displayAuthor: String
        get() = author.orEmpty().ifBlank { shareUser.orEmpty() }.ifBlank { "匿名作者" }

    val displayCategory: String
        get() = listOfNotNull(
            superChapterName?.takeIf { it.isNotBlank() },
            chapterName?.takeIf { it.isNotBlank() }
        ).joinToString(" / ").ifBlank { "--" }

    val displayDate: String
        get() = niceDate.orEmpty().ifBlank { niceShareDate.orEmpty() }.ifBlank { "--" }

    val safeLink: String
        get() = link.orEmpty()
}

data class ArticleTag(
    val name: String = "",
    val url: String = ""
)
