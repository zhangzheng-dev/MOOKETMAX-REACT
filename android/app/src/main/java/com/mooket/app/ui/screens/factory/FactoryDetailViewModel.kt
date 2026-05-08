package com.mooket.app.ui.screens.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.model.FactoryDetail
import com.mooket.app.data.model.FactoryProduct
import com.mooket.app.data.repository.MooketRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FactoryDetailUiState(
    val isLoading: Boolean = false,
    val factoryDetail: FactoryDetail? = null,  // 首次加载的完整数据（含看板统计）
    val error: String? = null,
    val selectedTab: Int = 0,  // 0: 报盘, 1: 求购
    val selectedSort: String = "comprehensive",  // comprehensive, price_asc, price_desc
    val isListRefreshing: Boolean = false,
    // 分页相关
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val currentProducts: List<FactoryProduct> = emptyList(),
    val hasMorePages: Boolean = false
)

class FactoryDetailViewModel : ViewModel() {

    private val repository = MooketRepository()

    private val _uiState = MutableStateFlow(FactoryDetailUiState())
    val uiState: StateFlow<FactoryDetailUiState> = _uiState.asStateFlow()

    private var currentCountry: String = ""
    private var currentFactoryNo: String = ""
    private var currentCategory: String = ""

    fun loadFactoryDetail(country: String, factoryNo: String, category: String) {
        currentCountry = country
        currentFactoryNo = factoryNo
        currentCategory = category

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val type = if (_uiState.value.selectedTab == 0) "offer" else "inquiry"
            val sortBy = _uiState.value.selectedSort

            val result = repository.getFactoryDetail(
                country = country,
                factoryNo = factoryNo,
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
                        factoryDetail = detail,
                        currentPage = 1,
                        currentProducts = detail.products,
                        hasMorePages = 1 < detail.totalPages
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadMoreProducts() {
        val state = _uiState.value
        state.factoryDetail ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }
            delay(50)

            val type = if (state.selectedTab == 0) "offer" else "inquiry"
            val nextPage = state.currentPage + 1

            repository.getFactoryDetail(
                country = currentCountry,
                factoryNo = currentFactoryNo,
                category = currentCategory,
                type = type,
                sortBy = state.selectedSort,
                page = nextPage,
                pageSize = state.pageSize
            ).onSuccess { newDetail ->
                _uiState.update {
                    val combinedProducts = it.currentProducts + newDetail.products
                    it.copy(
                        isLoadingMore = false,
                        currentPage = nextPage,
                        currentProducts = combinedProducts,
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
        loadMoreProducts()
    }

    fun selectTab(tab: Int) {
        if (_uiState.value.selectedTab != tab) {
            _uiState.update {
                it.copy(
                    selectedTab = tab,
                    currentPage = 1,
                    currentProducts = emptyList(),
                    hasMorePages = false
                )
            }
            // 只刷新列表，看板数据 factoryDetail 保持不变
            reloadProducts()
        }
    }

    fun selectSort(sort: String) {
        val currentSort = _uiState.value.selectedSort
        val newSort = when (sort) {
            "comprehensive" -> "comprehensive"
            "price" -> {
                // 价格排序：综合推荐 -> 升序 -> 降序 -> 综合推荐
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
                    currentProducts = emptyList(),
                    hasMorePages = false
                )
            }
            reloadProducts()
        }
    }

    private fun reloadProducts() {
        val state = _uiState.value
        viewModelScope.launch {
            val type = if (state.selectedTab == 0) "offer" else "inquiry"

            repository.getFactoryDetail(
                country = currentCountry,
                factoryNo = currentFactoryNo,
                category = currentCategory,
                type = type,
                sortBy = state.selectedSort,
                page = 1,
                pageSize = state.pageSize
            ).onSuccess { detail ->
                _uiState.update {
                    it.copy(
                        isListRefreshing = false,
                        // 只更新列表相关字段，保留 factoryDetail（看板统计）
                        currentPage = 1,
                        currentProducts = detail.products,
                        hasMorePages = 1 < detail.totalPages
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isListRefreshing = false, error = e.message) }
            }
        }
    }
}
