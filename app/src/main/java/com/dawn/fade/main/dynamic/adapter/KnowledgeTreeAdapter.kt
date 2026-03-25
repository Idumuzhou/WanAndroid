package com.dawn.fade.main.dynamic.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dawn.fade.R
import com.dawn.fade.data.model.knowledge.KnowledgeChildItem
import com.dawn.fade.data.model.knowledge.KnowledgeTreeItem
import com.dawn.fade.main.dynamic.widget.FlowLayout

/**
 * 体系树适配器，负责展示一级分类和全部可见的二级分类入口。
 */
class KnowledgeTreeAdapter(
    private val onChildClick: (KnowledgeTreeItem, KnowledgeChildItem) -> Unit
) : ListAdapter<KnowledgeTreeItem, RecyclerView.ViewHolder>(KnowledgeTreeDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_knowledge_tree, parent, false)
        return KnowledgeTreeViewHolder(itemView, onChildClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? KnowledgeTreeViewHolder)?.bind(getItem(position))
    }

    private class KnowledgeTreeViewHolder(
        itemView: View,
        private val onChildClick: (KnowledgeTreeItem, KnowledgeChildItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.textKnowledgeParentTitle)
        private val childFlowLayout: FlowLayout = itemView.findViewById(R.id.childKnowledgeFlowLayout)

        fun bind(item: KnowledgeTreeItem) {
            titleView.text = item.name
            syncChildViews(item)
        }

        private fun syncChildViews(item: KnowledgeTreeItem) {
            val context = itemView.context
            val existingViews = childFlowLayout.children.toList()
            val targetSize = item.children.size

            if (existingViews.size > targetSize) {
                for (index in existingViews.lastIndex downTo targetSize) {
                    childFlowLayout.removeViewAt(index)
                }
            }

            item.children.forEachIndexed { index, child ->
                val childView = (childFlowLayout.getChildAt(index) as? TextView)
                    ?: run {
                        val newView = LayoutInflater.from(context)
                            .inflate(R.layout.item_knowledge_child, childFlowLayout, false) as TextView
                        childFlowLayout.addView(newView)
                        newView
                    }

                childView.text = child.name
                childView.setOnClickListener { onChildClick(item, child) }
            }
        }
    }

    private class KnowledgeTreeDiffCallback : DiffUtil.ItemCallback<KnowledgeTreeItem>() {
        override fun areItemsTheSame(oldItem: KnowledgeTreeItem, newItem: KnowledgeTreeItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: KnowledgeTreeItem, newItem: KnowledgeTreeItem): Boolean {
            return oldItem == newItem
        }
    }
}
