package com.dawn.fade.main.home.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dawn.fade.R
import com.dawn.fade.data.model.home.BannerItem

/**
 * 首页 Banner 头部适配器，负责轮播图头部展示与自动滚动。
 */
class HomeBannerHeaderAdapter(
    private val onBannerClick: (BannerItem) -> Unit
) : RecyclerView.Adapter<HomeBannerHeaderAdapter.BannerHeaderViewHolder>() {
    private val banners = mutableListOf<BannerItem>()
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var currentHolder: BannerHeaderViewHolder? = null
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val holder = currentHolder ?: return
            val itemCount = holder.pagerAdapter.itemCount
            if (itemCount <= 1) {
                return
            }
            val nextItem = (holder.viewPager.currentItem + 1) % itemCount
            holder.viewPager.setCurrentItem(nextItem, true)
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY_MILLIS)
        }
    }

    override fun getItemCount(): Int {
        return if (banners.isEmpty()) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerHeaderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_banner_header, parent, false)
        return BannerHeaderViewHolder(itemView, onBannerClick)
    }

    override fun onBindViewHolder(holder: BannerHeaderViewHolder, position: Int) {
        currentHolder = holder
        holder.bind(banners)
        restartAutoScrollIfNeeded()
    }

    override fun onViewDetachedFromWindow(holder: BannerHeaderViewHolder) {
        stopAutoScroll()
        super.onViewDetachedFromWindow(holder)
    }

    fun submitBanners(newBanners: List<BannerItem>) {
        banners.clear()
        banners.addAll(newBanners)
        notifyDataSetChanged()
    }

    fun resumeAutoScroll() {
        restartAutoScrollIfNeeded()
    }

    fun stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    private fun restartAutoScrollIfNeeded() {
        stopAutoScroll()
        val holder = currentHolder ?: return
        if (holder.pagerAdapter.itemCount > 1) {
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MILLIS)
        }
    }

    class BannerHeaderViewHolder(
        itemView: View,
        onBannerClick: (BannerItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        val viewPager: ViewPager2 = itemView.findViewById(R.id.homeBannerViewPager)
        private val indicatorView: TextView = itemView.findViewById(R.id.textBannerIndicator)
        private val titleView: TextView = itemView.findViewById(R.id.textBannerTitle)
        val pagerAdapter = BannerPagerAdapter(onBannerClick)

        init {
            viewPager.adapter = pagerAdapter
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateIndicator(position)
                }
            })
        }

        fun bind(banners: List<BannerItem>) {
            pagerAdapter.submitList(banners)
            titleView.text = banners.firstOrNull()?.displayTitle.orEmpty()
            updateIndicator(viewPager.currentItem.coerceAtMost((banners.size - 1).coerceAtLeast(0)))
        }

        private fun updateIndicator(position: Int) {
            val items = pagerAdapter.currentItems
            if (items.isEmpty()) {
                indicatorView.text = ""
                titleView.text = ""
                return
            }
            val safePosition = position.coerceIn(0, items.lastIndex)
            indicatorView.text = itemView.context.getString(
                R.string.home_banner_indicator,
                safePosition + 1,
                items.size
            )
            titleView.text = items[safePosition].displayTitle
        }
    }

    private companion object {
        const val AUTO_SCROLL_DELAY_MILLIS = 3500L
    }
}
