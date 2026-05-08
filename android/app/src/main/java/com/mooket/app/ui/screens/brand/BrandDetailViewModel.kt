package com.mooket.app.ui.screens.brand

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.model.BrandDetail
import com.mooket.app.data.repository.MooketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BrandDetailUiState(
    val isLoading: Boolean = false,
    val brandDetail: BrandDetail? = null,
    val error: String? = null,
    val selectedTab: Int = 0,  // 0: 报盘, 1: 求购
    val selectedSort: String = "comprehensive",  // comprehensive, price_asc, price_desc
    val isListRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val currentSummaries: List<com.mooket.app.data.model.BrandProductSummary> = emptyList(),
    val hasMorePages: Boolean = false
)

class BrandDetailViewModel : ViewModel() {

    private val repository = MooketRepository()

    private val _uiState = MutableStateFlow(BrandDetailUiState())
    val uiState: StateFlow<BrandDetailUiState> = _uiState.asStateFlow()

    private var currentBrandName: String = ""
    private var currentCategory: String = ""

    fun loadBrandDetail(brandName: String, category: String) {
        currentBrandName = brandName
        currentCategory = category

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val type = if (_uiState.value.selectedTab == 0) "offer" else "inquiry"
            val sortBy = _uiState.value.selectedSort
            val result = repository.getBrandDetail(
                brandName = brandName,
                category = category,
                type = type,
                sortBy = sortBy,
                page = 1,
                pageSize = _uiState.value.pageSize
            )

            result.onSuccess { detail ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        brandDetail = detail,
                        currentPage = 1,
                        currentSummaries = detail.summaries,
                        hasMorePages = 1 < detail.totalPages
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadMoreSummaries() {
        val state = _uiState.value
        state.brandDetail ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }

            val type = if (state.selectedTab == 0) "offer" else "inquiry"
            val nextPage = state.currentPage + 1
            val sortBy = state.selectedSort

            repository.getBrandDetail(
                brandName = currentBrandName,
                category = currentCategory,
                type = type,
                sortBy = sortBy,
                page = nextPage,
                pageSize = state.pageSize
            ).onSuccess { newDetail ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        brandDetail = newDetail,
                        currentPage = nextPage,
                        currentSummaries = it.currentSummaries + newDetail.summaries,
                        hasMorePages = nextPage < newDetail.totalPages
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingMore = false, error = e.message) }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages) return
        loadMoreSummaries()
    }

    fun selectTab(tab: Int) {
        if (_uiState.value.selectedTab != tab) {
            _uiState.update {
                it.copy(
                    selectedTab = tab,
                    brandDetail = null,
                    currentPage = 1,
                    currentSummaries = emptyList(),
                    hasMorePages = false
                )
            }
            loadBrandDetail(currentBrandName, currentCategory)
        }
    }

    fun selectSort(sort: String) {
        val currentSort = _uiState.value.selectedSort
        val newSort = when (sort) {
            "comprehensive" -> "comprehensive"
            "price" -> {
                when (currentSort) {
                    "comprehensive" -> "price_asc"
                    "price_asc" -> "price_desc"
                    "price_desc" -> "comprehensive"
                    else -> "price_asc"
                }
            }
            else -> sort
        }
        if (currentSort != newSort) {
            _uiState.update {
                it.copy(
                    selectedSort = newSort,
                    isListRefreshing = true,
                    currentPage = 1,
                    currentSummaries = emptyList(),
                    hasMorePages = false
                )
            }
            reloadSummaries()
        }
    }

    private fun reloadSummaries() {
        val state = _uiState.value
        viewModelScope.launch {
            val type = if (state.selectedTab == 0) "offer" else "inquiry"
            val sortBy = state.selectedSort

            repository.getBrandDetail(
                brandName = currentBrandName,
                category = currentCategory,
                type = type,
                sortBy = sortBy,
                page = 1,
                pageSize = state.pageSize
            ).onSuccess { detail ->
                _uiState.update {
                    it.copy(
                        isListRefreshing = false,
                        brandDetail = detail,
                        currentPage = 1,
                        currentSummaries = detail.summaries,
                        hasMorePages = 1 < detail.totalPages
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isListRefreshing = false, error = e.message) }
            }
        }
    }
}
