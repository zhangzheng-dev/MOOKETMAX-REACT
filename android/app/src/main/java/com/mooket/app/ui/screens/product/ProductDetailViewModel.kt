package com.mooket.app.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.model.PriceTrend
import com.mooket.app.data.model.ProductDetail
import com.mooket.app.data.model.ProductSummary
import com.mooket.app.data.repository.MooketRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val product: ProductDetail? = null,
    val error: String? = null,
    val selectedTab: Int = 0,  // 0: 报盘, 1: 求购
    val selectedSort: String = "comprehensive",  // comprehensive, price_asc, price_desc
    // 列表刷新状态（用于排序切换时只刷新列表，不刷新整个页面）
    val isListRefreshing: Boolean = false,
    // 分页相关
    val isLoadingMore: Boolean = false,  // 是否正在加载更多
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val currentSummaries: List<ProductSummary> = emptyList(),  // 当前已加载的列表
    val hasMorePages: Boolean = false,  // 是否还有更多页
    // 趋势图相关
    val isTrendExpanded: Boolean = false,  // 趋势图是否展开
    val priceTrend: PriceTrend? = null,  // 价格趋势数据
    val isLoadingTrend: Boolean = false  // 是否正在加载趋势数据
)

class ProductDetailViewModel : ViewModel() {

    private val repository = MooketRepository()

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private var currentProductId: Int = 0
    private var currentCategory: String = ""

    fun loadProductDetail(productId: Int, category: String) {
        currentProductId = productId
        currentCategory = category

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val type = if (_uiState.value.selectedTab == 0) "offer" else "inquiry"
            val result = repository.getProductDetail(
                productId = productId,
                category = category,
                type = type,
                sortBy = _uiState.value.selectedSort,
                page = 1,
                pageSize = _uiState.value.pageSize
            )

            result.onSuccess { product ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        product = product,
                        currentPage = 1,
                        currentSummaries = product.summaries,
                        hasMorePages = 1 < product.totalPages
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadMoreSummaries() {
        val state = _uiState.value
        val product = state.product ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }
            delay(50)

            val type = if (state.selectedTab == 0) "offer" else "inquiry"
            val nextPage = state.currentPage + 1

            repository.getProductDetail(
                productId = currentProductId,
                category = currentCategory,
                type = type,
                sortBy = state.selectedSort,
                page = nextPage,
                pageSize = state.pageSize
            ).onSuccess { newProduct ->
                _uiState.update {
                    val combinedSummaries = it.currentSummaries + newProduct.summaries
                    it.copy(
                        isLoadingMore = false,
                        product = newProduct,
                        currentPage = nextPage,
                        currentSummaries = combinedSummaries,
                        hasMorePages = nextPage < newProduct.totalPages
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
                    product = null,
                    currentPage = 1,
                    currentSummaries = emptyList(),
                    hasMorePages = false
                )
            }
            loadProductDetail(currentProductId, currentCategory)
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

            repository.getProductDetail(
                productId = currentProductId,
                category = currentCategory,
                type = type,
                sortBy = sortBy,
                page = 1,
                pageSize = state.pageSize
            ).onSuccess { product ->
                _uiState.update {
                    it.copy(
                        isListRefreshing = false,
                        product = product,
                        currentPage = 1,
                        currentSummaries = product.summaries,
                        hasMorePages = 1 < product.totalPages
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isListRefreshing = false, error = e.message) }
            }
        }
    }

    fun toggleTrendExpanded() {
        val state = _uiState.value
        val newExpanded = !state.isTrendExpanded
        _uiState.update { it.copy(isTrendExpanded = newExpanded) }

        // 如果展开且还没有加载趋势数据，则加载
        if (newExpanded && state.priceTrend == null) {
            loadPriceTrend()
        }
    }

    fun loadPriceTrend() {
        val product = _uiState.value.product ?: return
        val offerType = if (_uiState.value.selectedTab == 0) "报盘" else "求购"

        // 获取国家信息（从第一个汇总项）
        val country = product.summaries.firstOrNull()?.country ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrend = true) }

            val result = repository.getPriceTrend(
                type = "country_product",
                country = country,
                productId = product.productId,
                factoryNo = null,
                offerType = offerType
            )

            result.onSuccess { trend ->
                _uiState.update { it.copy(isLoadingTrend = false, priceTrend = trend) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingTrend = false) }
            }
        }
    }
}
