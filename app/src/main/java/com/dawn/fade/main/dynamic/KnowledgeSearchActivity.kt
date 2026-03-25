package com.dawn.fade.main.dynamic

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * 作者搜索页，负责按照作者昵称搜索文章。
 */
class KnowledgeSearchActivity : BaseActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var authorInput: TextInputEditText
    private lateinit var searchButton: MaterialButton
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
        setContentView(R.layout.activity_knowledge_search)

        toolbar = findViewById(R.id.knowledgeSearchToolbar)
        authorInput = findViewById(R.id.inputKnowledgeSearchAuthor)
        searchButton = findViewById(R.id.buttonKnowledgeSearch)
        swipeRefreshLayout = findViewById(R.id.knowledgeSearchSwipeRefreshLayout)
        recyclerView = findViewById(R.id.knowledgeSearchRecyclerView)
        loadingView = findViewById(R.id.knowledgeSearchInitialLoadingView)
        emptyView = findViewById(R.id.knowledgeSearchEmptyView)

        toolbar.title = getString(R.string.knowledge_search_page_title)
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

        searchButton.setOnClickListener {
            search()
        }
        authorInput.setOnEditorActionListener { _, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (isSearchAction || isEnterKey) {
                search()
                true
            } else {
                false
            }
        }
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.message.collect { message ->
                        Toast.makeText(this@KnowledgeSearchActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun search() {
        val keyword = authorInput.text?.toString().orEmpty().trim()
        if (keyword.isBlank()) {
            Toast.makeText(this, R.string.knowledge_search_author_required, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.searchByAuthor(keyword)
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
}
