package com.dawn.fade.main.dynamic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dawn.fade.BaseActivity
import com.dawn.fade.R
import com.dawn.fade.main.article.adapter.ArticleListAdapter
import com.dawn.fade.main.dynamic.ui.KnowledgeArticleListUiState
import com.dawn.fade.main.dynamic.ui.KnowledgeArticleListViewModel
import com.dawn.fade.web.WebViewActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

/**
 * 体系分类文章页，负责展示某个二级分类下的文章列表。
 */
class KnowledgeArticleActivity : BaseActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var subtitleView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var articleAdapter: ArticleListAdapter

    private val viewModel: KnowledgeArticleListViewModel by lazy {
        ViewModelProvider(this)[KnowledgeArticleListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knowledge_article)

        toolbar = findViewById(R.id.knowledgeArticleToolbar)
        subtitleView = findViewById(R.id.textKnowledgeArticleSubtitle)
        swipeRefreshLayout = findViewById(R.id.knowledgeArticleSwipeRefreshLayout)
        recyclerView = findViewById(R.id.knowledgeArticleRecyclerView)
        loadingView = findViewById(R.id.knowledgeArticleInitialLoadingView)
        emptyView = findViewById(R.id.knowledgeArticleEmptyView)

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val parentName = intent.getStringExtra(EXTRA_PARENT_NAME).orEmpty()
        val cid = intent.getIntExtra(EXTRA_CID, -1)

        toolbar.title = title
        subtitleView.text = parentName
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        articleAdapter = ArticleListAdapter { article ->
            if (article.safeLink.isBlank()) {
                Toast.makeText(this, R.string.web_link_unavailable, Toast.LENGTH_SHORT).show()
                return@ArticleListAdapter
            }
            startActivity(
                WebViewActivity.createIntent(
                    context = this,
                    title = article.displayTitle,
                    url = article.safeLink
                )
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = articleAdapter
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null
        recyclerView.setItemViewCacheSize(12)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                if (layoutManager.findLastVisibleItemPosition() >= articleAdapter.itemCount - 3) {
                    viewModel.loadMore()
                }
            }
        })
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }

        if (cid != -1) {
            viewModel.setupCategory(cid)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.message.collect { message ->
                        Toast.makeText(this@KnowledgeArticleActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun render(state: KnowledgeArticleListUiState) {
        swipeRefreshLayout.isRefreshing = state.isRefreshing
        loadingView.isVisible = state.isInitialLoading
        emptyView.isVisible = state.hasRequested && state.articles.isEmpty() && !state.isInitialLoading
        articleAdapter.submitState(
            articles = state.articles,
            isLoadingMore = state.isLoadingMore,
            canLoadMore = state.canLoadMore
        )
    }

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_PARENT_NAME = "extra_parent_name"
        private const val EXTRA_CID = "extra_cid"

        fun createIntent(context: Context, title: String, parentName: String, cid: Int): Intent {
            return Intent(context, KnowledgeArticleActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_PARENT_NAME, parentName)
                putExtra(EXTRA_CID, cid)
            }
        }
    }
}
