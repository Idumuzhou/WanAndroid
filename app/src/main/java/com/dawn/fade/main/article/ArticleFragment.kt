package com.dawn.fade.main.article

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dawn.fade.R
import com.dawn.fade.main.article.adapter.ArticleListAdapter
import com.dawn.fade.main.article.ui.ArticleListUiState
import com.dawn.fade.main.article.ui.ArticleListViewModel
import com.dawn.fade.web.WebViewActivity
import kotlinx.coroutines.launch

/**
 * 文章 Fragment，负责文章列表展示、分页刷新和详情跳转。
 */
class ArticleFragment : Fragment(R.layout.fragment_article) {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var articleAdapter: ArticleListAdapter

    private val viewModel: ArticleListViewModel by lazy {
        ViewModelProvider(this)[ArticleListViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.articleSwipeRefreshLayout)
        recyclerView = view.findViewById(R.id.articleRecyclerView)
        loadingView = view.findViewById(R.id.articleInitialLoadingView)
        emptyView = view.findViewById(R.id.articleEmptyView)

        articleAdapter = ArticleListAdapter { article ->
            if (article.safeLink.isBlank()) {
                Toast.makeText(requireContext(), R.string.web_link_unavailable, Toast.LENGTH_SHORT).show()
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
        recyclerView.adapter = articleAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) {
                    return
                }

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItemPosition >= articleAdapter.itemCount - 3) {
                    viewModel.loadMore()
                }
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect(::render)
                }
                launch {
                    viewModel.message.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun render(state: ArticleListUiState) {
        swipeRefreshLayout.isRefreshing = state.isRefreshing
        loadingView.visibility = if (state.isInitialLoading) View.VISIBLE else View.GONE
        emptyView.visibility = if (state.articles.isEmpty() && !state.isInitialLoading) {
            View.VISIBLE
        } else {
            View.GONE
        }

        articleAdapter.submitState(
            articles = state.articles,
            isLoadingMore = state.isLoadingMore,
            canLoadMore = state.canLoadMore
        )
    }
}
