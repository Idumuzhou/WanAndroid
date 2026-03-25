package com.dawn.fade.main.dynamic

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dawn.fade.R
import com.dawn.fade.main.dynamic.adapter.KnowledgeTreeAdapter
import com.dawn.fade.main.dynamic.ui.KnowledgeUiState
import com.dawn.fade.main.dynamic.ui.KnowledgeViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * 体系 Fragment，负责展示知识体系结构和搜索入口。
 */
class DynamicFragment : Fragment(R.layout.fragment_dynamic) {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var searchEntryCardView: View
    private lateinit var searchFieldLayout: View
    private lateinit var searchEntryButton: MaterialButton
    private lateinit var adapter: KnowledgeTreeAdapter

    private val viewModel: KnowledgeViewModel by lazy {
        ViewModelProvider(this)[KnowledgeViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.knowledgeSwipeRefreshLayout)
        recyclerView = view.findViewById(R.id.knowledgeRecyclerView)
        loadingView = view.findViewById(R.id.knowledgeInitialLoadingView)
        emptyView = view.findViewById(R.id.knowledgeEmptyView)
        searchEntryCardView = view.findViewById(R.id.cardKnowledgeSearchEntry)
        searchFieldLayout = view.findViewById(R.id.layoutKnowledgeSearchField)
        searchEntryButton = view.findViewById(R.id.buttonOpenKnowledgeSearch)

        adapter = KnowledgeTreeAdapter { parent, child ->
            startActivity(
                KnowledgeArticleActivity.createIntent(
                    context = requireContext(),
                    title = child.name,
                    parentName = parent.name,
                    cid = child.id
                )
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null
        recyclerView.setItemViewCacheSize(12)

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
        val openSearchPage = {
            startActivity(Intent(requireContext(), KnowledgeSearchActivity::class.java))
        }
        searchEntryCardView.setOnClickListener { openSearchPage() }
        searchFieldLayout.setOnClickListener { openSearchPage() }
        searchEntryButton.setOnClickListener { openSearchPage() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.message.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun render(state: KnowledgeUiState) {
        swipeRefreshLayout.isRefreshing = state.isRefreshing
        loadingView.isVisible = state.isInitialLoading
        emptyView.isVisible = state.knowledgeTree.isEmpty() && !state.isInitialLoading
        adapter.submitList(state.knowledgeTree)
    }
}
