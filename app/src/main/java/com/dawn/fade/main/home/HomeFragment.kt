package com.dawn.fade.main.home

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dawn.fade.R
import com.dawn.fade.main.article.adapter.ArticleListAdapter
import com.dawn.fade.main.home.adapter.HomeBannerHeaderAdapter
import com.dawn.fade.main.home.ui.HomeUiState
import com.dawn.fade.main.home.ui.HomeViewModel
import com.dawn.fade.web.WebViewActivity
import kotlinx.coroutines.launch

/**
 * 首页 Fragment，负责展示 Banner、文章流和刷新分页逻辑。
 */
class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var bannerHeaderAdapter: HomeBannerHeaderAdapter
    private lateinit var articleListAdapter: ArticleListAdapter

    private val viewModel: HomeViewModel by lazy {
        ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.homeSwipeRefreshLayout)
        recyclerView = view.findViewById(R.id.homeRecyclerView)
        loadingView = view.findViewById(R.id.homeInitialLoadingView)
        emptyView = view.findViewById(R.id.homeEmptyView)

        bannerHeaderAdapter = HomeBannerHeaderAdapter { banner ->
            if (banner.safeUrl.isBlank()) {
                toast(R.string.web_link_unavailable)
                return@HomeBannerHeaderAdapter
            }
            startActivity(
                WebViewActivity.createIntent(
                    context = requireContext(),
                    title = banner.displayTitle,
                    url = banner.safeUrl
                )
            )
        }
        articleListAdapter = ArticleListAdapter { article ->
            if (article.safeLink.isBlank()) {
                toast(R.string.web_link_unavailable)
                return@ArticleListAdapter
            }
            startActivity(
                WebViewActivity.createIntent(
                    context = requireContext(),
                    title = article.displayTitle,
                    url = article.safeLink
                )
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ConcatAdapter(bannerHeaderAdapter, articleListAdapter)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) {
                    return
                }

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItemPosition >= recyclerView.adapter.orEmptyItemCount() - 3) {
                    viewModel.loadMore()
                }
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.message.collect { message ->
                        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onPause() {
        bannerHeaderAdapter.stopAutoScroll()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        bannerHeaderAdapter.resumeAutoScroll()
    }

    override fun onDestroyView() {
        bannerHeaderAdapter.stopAutoScroll()
        recyclerView.adapter = null
        super.onDestroyView()
    }

    private fun render(state: HomeUiState) {
        swipeRefreshLayout.isRefreshing = state.isRefreshing
        loadingView.visibility = if (state.isInitialLoading) View.VISIBLE else View.GONE
        emptyView.visibility = if (
            state.banners.isEmpty() && state.articles.isEmpty() && !state.isInitialLoading
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }

        bannerHeaderAdapter.submitBanners(state.banners)
        articleListAdapter.submitState(
            articles = state.articles,
            isLoadingMore = state.isLoadingMore,
            canLoadMore = state.canLoadMore
        )
    }

    private fun toast(messageRes: Int) {
        android.widget.Toast.makeText(requireContext(), messageRes, android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun RecyclerView.Adapter<*>?.orEmptyItemCount(): Int {
    return this?.itemCount ?: 0
}
