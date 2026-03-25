package com.dawn.fade.main.dynamic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dawn.fade.data.model.base.ApiResult
import com.dawn.fade.data.repository.Repository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 体系页 ViewModel，负责拉取知识体系树结构。
 */
class KnowledgeViewModel(
    private val repository: Repository = Repository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(KnowledgeUiState())
    val uiState: StateFlow<KnowledgeUiState> = _uiState.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        val currentState = _uiState.value
        if (currentState.isRefreshing) {
            return
        }

        _uiState.value = if (currentState.knowledgeTree.isEmpty()) {
            currentState.copy(isInitialLoading = true, isRefreshing = false)
        } else {
            currentState.copy(isInitialLoading = false, isRefreshing = true)
        }

        viewModelScope.launch {
            when (val result = repository.fetchKnowledgeTree()) {
                is ApiResult.Success -> {
                    _uiState.value = KnowledgeUiState(
                        knowledgeTree = result.data,
                        isInitialLoading = false,
                        isRefreshing = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isInitialLoading = false,
                        isRefreshing = false
                    )
                    _message.emit(result.message)
                }
            }
        }
    }
}
