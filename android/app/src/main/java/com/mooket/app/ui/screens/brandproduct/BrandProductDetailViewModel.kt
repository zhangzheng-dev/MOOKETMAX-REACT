package com.mooket.app.ui.screens.brandproduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.model.BrandProductDetailResult
import com.mooket.app.data.model.BrandProductSummary
import com.mooket.app.data.repository.MooketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BrandProductDetailUiState(
    val isLoading: Boolean = false,
    val brandDetail: BrandProductDetailResult? = null,
    val error: String? = null,
    val selectedTab: Int = 0,  // 0: 报盘, 1: 求购
    val selectedSort: String = "comprehensive",  // comprehensive, price_asc, price_desc
    // 列表刷新状态（用于排序切换时只刷新列表，不刷新整个页面）
    val isListRefreshing: Boolean = false,
    // 分页相关
    val isLoadingMore: Boolean = false,  // 是否正在加载更多
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val currentSummaries: List<BrandProductSummary> = emptyList(),  // 当前已加载的列表
    val hasMorePages: Boolean = false  // 是否还有更多页
)

class BrandProductDetailViewModel : ViewModel() {

    private val repository = MooketRepository()

    private val _uiState = MutableStateFlow(BrandProductDetailUiState())
    val uiState: StateFlow<BrandProductDetailUiState> = _uiState.asStateFlow()

    private var currentBrandName: String = ""
    private var currentProductName: String = ""
    private var currentCategory: String = ""

    fun loadBrandProductDetail(brandName: String, productName: String, category: String) {
        currentBrandName = brandName
        currentProductName = productName
        currentCategory = category

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val type = if (_uiState.value.selectedTab == 0) "offer" else "inquiry"
            val result = repository.getBrandProductDetail(
                brandName = brandName,
                productName = productName,
                category = category,
                type = type,
                sortBy = _uiState.value.selectedSort,
                page = 1,
                pageSize = _uiState.value.pageSize
            )

            result.onSuccess { brandDetail ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        brandDetail = brandDetail,
                        currentPage = 1,
                        currentSummaries = brandDetail.summaries,
                        hasMorePages = 1 < brandDetail.totalPages
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadMoreSummaries() {
        val state = _uiState.value
        if (state.brandDetail == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }

            val type = if (state.selectedTab == 0) "offer" else "inquiry"
            val nextPage = state.currentPage + 1

            repository.getBrandProductDetail(
                brandName = currentBrandName,
                productName = currentProductName,
                category = currentCategory,
                type = type,
                sortBy = state.selectedSort,
                page = nextPage,
                pageSize = state.pageSize
            ).onSuccess { newBrandDetail ->
                _uiState.update {
                    val combinedSummaries = it.currentSummaries + newBrandDetail.summaries
                    it.copy(
                        isLoadingMore = false,
                        brandDetail = newBrandDetail,
                        currentPage = nextPage,
                        currentSummaries = combinedSummaries,
                        hasMorePages = nextPage < newBrandDetail.totalPages
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
            loadBrandProductDetail(currentBrandName, currentProductName, currentCategory)
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

            repository.getBrandProductDetail(
                brandName = currentBrandName,
                productName = currentProductName,
                category = currentCategory,
                type = type,
                sortBy = sortBy,
                page = 1,
                pageSize = state.pageSize
            ).onSuccess { brandDetail ->
                _uiState.update {
                    it.copy(
                        isListRefreshing = false,
                        brandDetail = brandDetail,
                        currentPage = 1,
                        currentSummaries = brandDetail.summaries,
                        hasMorePages = 1 < brandDetail.totalPages
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isListRefreshing = false, error = e.message) }
            }
        }
    }
}
