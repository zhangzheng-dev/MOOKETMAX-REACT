package com.mooket.app.ui.screens.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.model.FactoryFilter
import com.mooket.app.data.model.MerchantDetail
import com.mooket.app.data.model.OfferSummary
import com.mooket.app.data.repository.MooketRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MerchantUiState(
    val isLoading: Boolean = false,
    val merchant: MerchantDetail? = null,
    val error: String? = null,
    val selectedTab: Int = 0,  // 0: 报盘, 1: 求购
    val expandedOfferId: Long? = null,
    // 分页相关
    val isLoadingMore: Boolean = false,  // 是否正在加载更多
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val totalPages: Int = 1,
    val currentProducts: List<OfferSummary> = emptyList(),  // 当前已加载的产品列表
    val hasMorePages: Boolean = false  // 是否还有更多页
)

class MerchantViewModel : ViewModel() {

    private val repository = MooketRepository()
    private var currentMerchantId: Long = 0
    private var currentCategory: String = ""

    private val _uiState = MutableStateFlow(MerchantUiState())
    val uiState: StateFlow<MerchantUiState> = _uiState.asStateFlow()

    fun loadMerchantDetail(merchantId: Long, category: String) {
        currentMerchantId = merchantId
        currentCategory = category

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getMerchantDetail(merchantId, category)
                .onSuccess { merchant ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            merchant = merchant,
                            expandedOfferId = null,
                            currentPage = 1,
                            currentProducts = emptyList()
                        )
                    }
                    // 加载第一页产品
                    loadProducts()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    private fun loadProducts() {
        val tab = _uiState.value.selectedTab
        val offerType = if (tab == 0) "offer" else "inquiry"

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }
            delay(50)

            repository.getMerchantProducts(
                merchantId = currentMerchantId,
                type = offerType,
                category = currentCategory,
                page = _uiState.value.currentPage,
                pageSize = _uiState.value.pageSize
            ).onSuccess { page ->
                _uiState.update { state ->
                    val newProducts = if (state.currentPage == 1) {
                        page.products
                    } else {
                        state.currentProducts + page.products
                    }
                    state.copy(
                        isLoadingMore = false,
                        currentProducts = newProducts,
                        totalPages = page.totalPages,
                        hasMorePages = state.currentPage < page.totalPages
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingMore = false, error = e.message) }
            }
        }
    }

    fun loadMoreProducts() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages) return

        _uiState.update { it.copy(currentPage = it.currentPage + 1, isLoadingMore = true) }
        loadProducts()
    }

    suspend fun getFactoryFilter(): FactoryFilter? {
        return repository.getFactoryFilter().getOrNull()
    }

    fun selectTab(tab: Int) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                expandedOfferId = null,
                currentPage = 1,
                currentProducts = emptyList(),
                hasMorePages = false
            )
        }
        loadProducts()
    }

    fun toggleOfferExpand(offerId: Long?) {
        _uiState.update {
            it.copy(expandedOfferId = if (it.expandedOfferId == offerId) null else offerId)
        }
    }
}
