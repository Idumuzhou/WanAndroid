package com.dawn.fade.main.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dawn.fade.R
import com.dawn.fade.data.model.home.BannerItem

/**
 * Banner Pager 适配器，负责展示首页轮播图图片并响应点击。
 */
class BannerPagerAdapter(
    private val onBannerClick: (BannerItem) -> Unit
) : RecyclerView.Adapter<BannerPagerAdapter.BannerPagerViewHolder>() {
    private val banners = mutableListOf<BannerItem>()
    val currentItems: List<BannerItem>
        get() = banners

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerPagerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_banner_page, parent, false)
        return BannerPagerViewHolder(itemView)
    }

    override fun getItemCount(): Int = banners.size

    override fun onBindViewHolder(holder: BannerPagerViewHolder, position: Int) {
        val banner = banners[position]
        holder.bind(banner)
        holder.itemView.setOnClickListener {
            onBannerClick(banner)
        }
    }

    fun submitList(items: List<BannerItem>) {
        banners.clear()
        banners.addAll(items)
        notifyDataSetChanged()
    }

    class BannerPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageBanner)

        fun bind(item: BannerItem) {
            Glide.with(imageView)
                .load(item.safeImagePath)
                .placeholder(R.drawable.bg_banner_placeholder)
                .error(R.drawable.bg_banner_placeholder)
                .centerCrop()
                .into(imageView)
        }
    }
}
