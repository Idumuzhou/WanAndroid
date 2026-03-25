package com.dawn.fade.main.article.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dawn.fade.R
import com.dawn.fade.data.model.article.ArticleItem

/**
 * 文章列表适配器，使用 DiffUtil 增量刷新文章项与底部状态项。
 */
class ArticleListAdapter(
    private val onArticleClick: (ArticleItem) -> Unit
) : ListAdapter<ArticleListAdapter.DisplayItem, RecyclerView.ViewHolder>(DisplayItemDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).stableId
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DisplayItem.ArticleRow -> VIEW_TYPE_ARTICLE
            is DisplayItem.FooterRow -> VIEW_TYPE_FOOTER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_ARTICLE) {
            ArticleViewHolder(inflater.inflate(R.layout.item_article, parent, false))
        } else {
            FooterViewHolder(inflater.inflate(R.layout.item_article_footer, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is DisplayItem.ArticleRow -> {
                (holder as? ArticleViewHolder)?.bind(item.article)
                holder.itemView.setOnClickListener { onArticleClick(item.article) }
            }
            is DisplayItem.FooterRow -> {
                (holder as? FooterViewHolder)?.bind(
                    isLoadingMore = item.isLoadingMore,
                    canLoadMore = item.canLoadMore
                )
            }
        }
    }

    fun submitState(
        articles: List<ArticleItem>,
        isLoadingMore: Boolean,
        canLoadMore: Boolean
    ) {
        val items = buildList {
            articles.forEach { add(DisplayItem.ArticleRow(it)) }
            if (articles.isNotEmpty()) {
                add(DisplayItem.FooterRow(isLoadingMore = isLoadingMore, canLoadMore = canLoadMore))
            }
        }
        submitList(items)
    }

    sealed interface DisplayItem {
        val stableId: Long

        data class ArticleRow(val article: ArticleItem) : DisplayItem {
            override val stableId: Long = article.id.toLong()
        }

        data class FooterRow(
            val isLoadingMore: Boolean,
            val canLoadMore: Boolean
        ) : DisplayItem {
            override val stableId: Long = Long.MIN_VALUE
        }
    }

    private class DisplayItemDiffCallback : DiffUtil.ItemCallback<DisplayItem>() {
        override fun areItemsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
            return oldItem::class == newItem::class && oldItem.stableId == newItem.stableId
        }

        override fun areContentsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
            return oldItem == newItem
        }
    }

    private class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val badgeView: TextView = itemView.findViewById(R.id.textArticleBadge)
        private val titleView: TextView = itemView.findViewById(R.id.textArticleTitle)
        private val authorView: TextView = itemView.findViewById(R.id.textArticleAuthor)
        private val categoryView: TextView = itemView.findViewById(R.id.textArticleCategory)
        private val dateView: TextView = itemView.findViewById(R.id.textArticleDate)

        fun bind(article: ArticleItem) {
            val context = itemView.context
            val badgeText = when {
                article.fresh -> context.getString(R.string.article_badge_fresh)
                article.tags?.isNotEmpty() == true -> article.tags.first().name
                else -> ""
            }.orEmpty()

            badgeView.isVisible = badgeText.isNotBlank()
            badgeView.text = badgeText
            titleView.text = article.displayTitle
            authorView.text = context.getString(R.string.article_by_author, article.displayAuthor)
            categoryView.text = article.displayCategory
            dateView.text = article.displayDate
        }
    }

    private class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val progressView: ProgressBar = itemView.findViewById(R.id.articleFooterProgress)
        private val textView: TextView = itemView.findViewById(R.id.articleFooterText)

        fun bind(isLoadingMore: Boolean, canLoadMore: Boolean) {
            val context = itemView.context
            when {
                isLoadingMore -> {
                    progressView.visibility = View.VISIBLE
                    textView.text = context.getString(R.string.article_load_more_loading)
                }
                canLoadMore -> {
                    progressView.visibility = View.GONE
                    textView.text = context.getString(R.string.article_load_more_idle)
                }
                else -> {
                    progressView.visibility = View.GONE
                    textView.text = context.getString(R.string.article_load_more_finished)
                }
            }
        }
    }

    private companion object {
        const val VIEW_TYPE_ARTICLE = 1
        const val VIEW_TYPE_FOOTER = 2
    }
}
