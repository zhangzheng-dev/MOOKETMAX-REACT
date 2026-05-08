package com.mooket.app.ui.screens.substitute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.api.RetrofitClient
import com.mooket.app.data.model.MerchantOption
import com.mooket.app.data.model.SubstituteProduct
import com.mooket.app.data.model.SubstituteProductDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 平替产品 ViewModel
 */
class SubstituteProductViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow(SubstituteProductUiState())
    val uiState: StateFlow<SubstituteProductUiState> = _uiState.asStateFlow()

    private var currentCountry: String = ""
    private var currentProductName: String = ""
    private var currentCategory: String = "牛"
    private var originalFactoryNo: String = ""
    private var originalPriceMin: Double? = null
    private var originalPriceMax: Double? = null

    /**
     * 加载数据
     */
    fun loadData(country: String, factoryNo: String, productName: String, category: String) {
        currentCountry = country
        currentProductName = productName
        currentCategory = category
        originalFactoryNo = factoryNo

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 加载平替产品列表
                val listResponse = apiService.getSubstituteProducts(country, factoryNo, productName, category)
                if (listResponse.code == 200 && listResponse.data != null) {
                    val sp = listResponse.data
                    // 过滤掉原始厂号
                    val filteredFactories = sp.factories.filter { it.factoryNo != originalFactoryNo }
                    // 默认选中第一个平替产品
                    val selectedFactory = filteredFactories.firstOrNull()?.factoryNo ?: factoryNo
                    _uiState.value = _uiState.value.copy(
                        substituteProduct = sp.copy(factories = filteredFactories),
                        selectedFactoryNo = selectedFactory
                    )

                    // 先加载原始价格
                    val originalResponse = apiService.getSubstituteProductDetail(
                        country = currentCountry,
                        factoryNo = originalFactoryNo,
                        productName = currentProductName,
                        category = currentCategory,
                        type = "offer",
                        sortBy = "comprehensive",
                        page = 1,
                        pageSize = 1
                    )
                    if (originalResponse.code == 200 && originalResponse.data != null) {
                        originalPriceMin = originalResponse.data.priceMin
                        originalPriceMax = originalResponse.data.priceMax
                    }

                    // 再加载平替产品详情
                    loadDetail(selectedFactory, isInitial = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = listResponse.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "加载失败")
            }
        }
    }

    /**
     * 选择厂号
     */
    fun selectFactory(factoryNo: String) {
        if (_uiState.value.selectedFactoryNo != factoryNo) {
            _uiState.value = _uiState.value.copy(
                selectedFactoryNo = factoryNo,
                page = 1,
                priceMin = null,
                priceMax = null,
                goodsTypes = emptySet(),
                feedingTypes = emptySet(),
                tags = emptySet(),
                isFamousMerchant = false,
                selectedMerchants = emptySet(),
                regions = emptySet(),
                isListLoading = true
            )
            loadDetail(factoryNo, isInitial = false)
        }
    }

    /**
     * 加载详情
     * @param isInitial 是否是初始加载（影响使用 isLoading 还是 isListLoading）
     */
    private fun loadDetail(factoryNo: String, isInitial: Boolean) {
        viewModelScope.launch {
            try {
                val response = apiService.getSubstituteProductDetail(
                    country = currentCountry,
                    factoryNo = factoryNo,
                    productName = currentProductName,
                    category = currentCategory,
                    type = _uiState.value.offerType,
                    sortBy = _uiState.value.sortBy,
                    page = _uiState.value.page,
                    pageSize = 20
                )

                if (response.code == 200 && response.data != null) {
                    val detail = response.data
                    // 从商家报盘中提取商家选项
                    val merchantOptions = detail.merchantOffers
                        .mapNotNull { group -> group.merchantId?.let { id -> MerchantOption(id, group.merchantName ?: "未知商家") } }
                        .distinctBy { it.id }

                    // 从商家报盘中提取地区选项
                    val regionOptions = detail.merchantOffers
                        .mapNotNull { it.employeeOffers }
                        .flatten()
                        .mapNotNull { it.goodsLocation }
                        .distinct()

                    // 用原始价格覆盖（用于看板显示）
                    val finalDetail = detail.copy(
                        priceMin = originalPriceMin,
                        priceMax = originalPriceMax
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = if (isInitial) false else _uiState.value.isLoading,
                        isListLoading = if (isInitial) _uiState.value.isListLoading else false,
                        detail = finalDetail,
                        merchantOptions = merchantOptions,
                        regionOptions = regionOptions,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = if (isInitial) false else _uiState.value.isLoading,
                        isListLoading = if (isInitial) _uiState.value.isListLoading else false,
                        error = response.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = if (isInitial) false else _uiState.value.isLoading,
                    isListLoading = if (isInitial) _uiState.value.isListLoading else false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    /**
     * 切换排序方式
     */
    fun switchSortBy(sortBy: String) {
        if (_uiState.value.sortBy != sortBy) {
            _uiState.value = _uiState.value.copy(sortBy = sortBy, page = 1, isListLoading = true)
            loadDetail(_uiState.value.selectedFactoryNo, isInitial = false)
        }
    }

    /**
     * 切换报盘类型
     */
    fun switchOfferType(type: String) {
        if (_uiState.value.offerType != type) {
            _uiState.value = _uiState.value.copy(offerType = type, page = 1, isListLoading = true)
            loadDetail(_uiState.value.selectedFactoryNo, isInitial = false)
        }
    }

    /**
     * 切换知名商家筛选
     */
    fun toggleFamousMerchant() {
        _uiState.value = _uiState.value.copy(isFamousMerchant = !_uiState.value.isFamousMerchant)
    }

    /**
     * 切换商家筛选
     */
    fun toggleMerchant(merchantId: Long) {
        val current = _uiState.value.selectedMerchants
        val updated = if (current.contains(merchantId)) {
            current - merchantId
        } else {
            current + merchantId
        }
        _uiState.value = _uiState.value.copy(selectedMerchants = updated)
    }

    /**
     * 切换地区筛选
     */
    fun toggleRegion(region: String) {
        val current = _uiState.value.regions
        val updated = if (current.contains(region)) {
            current - region
        } else {
            current + region
        }
        _uiState.value = _uiState.value.copy(regions = updated)
    }

    /**
     * 设置价格区间筛选
     */
    fun setPriceRange(min: String?, max: String?) {
        _uiState.value = _uiState.value.copy(priceMin = min, priceMax = max)
    }

    /**
     * 切换货物类型筛选
     */
    fun toggleGoodsType(type: String) {
        val current = _uiState.value.goodsTypes
        val updated = if (current.contains(type)) {
            current - type
        } else {
            current + type
        }
        _uiState.value = _uiState.value.copy(goodsTypes = updated)
    }

    /**
     * 切换饲养方式筛选
     */
    fun toggleFeedingType(type: String) {
        val current = _uiState.value.feedingTypes
        val updated = if (current.contains(type)) {
            current - type
        } else {
            current + type
        }
        _uiState.value = _uiState.value.copy(feedingTypes = updated)
    }

    /**
     * 切换标签筛选
     */
    fun toggleTag(tag: String) {
        val current = _uiState.value.tags
        val updated = if (current.contains(tag)) {
            current - tag
        } else {
            current + tag
        }
        _uiState.value = _uiState.value.copy(tags = updated)
    }

    /**
     * 切换筛选（用于显示）
     */
    fun toggleFilter(filter: String) {
        val current = _uiState.value.activeFilters
        val updated = if (current.contains(filter)) {
            current - filter
        } else {
            current + filter
        }
        _uiState.value = _uiState.value.copy(activeFilters = updated)
    }

    /**
     * 清除筛选
     */
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            activeFilters = emptySet(),
            priceMin = null,
            priceMax = null,
            goodsTypes = emptySet(),
            feedingTypes = emptySet(),
            tags = emptySet(),
            isFamousMerchant = false,
            selectedMerchants = emptySet(),
            regions = emptySet()
        )
    }

    /**
     * 加载更多
     */
    fun loadMore() {
        val detail = _uiState.value.detail ?: return
        if (_uiState.value.isListLoading) return
        if (detail.page >= detail.totalPages) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isListLoading = true)

            try {
                val nextPage = detail.page + 1
                val response = apiService.getSubstituteProductDetail(
                    country = currentCountry,
                    factoryNo = _uiState.value.selectedFactoryNo,
                    productName = currentProductName,
                    category = currentCategory,
                    type = _uiState.value.offerType,
                    sortBy = _uiState.value.sortBy,
                    page = nextPage,
                    pageSize = 20
                )

                if (response.code == 200 && response.data != null) {
                    val newData = response.data
                    // 合并新旧列表
                    val mergedOffers = (detail.merchantOffers + newData.merchantOffers)
                        .groupBy { it.merchantId }
                        .map { (_, groups) ->
                            if (groups.size == 1) groups.first()
                            else groups.reduce { acc, g ->
                                com.mooket.app.data.model.MerchantOfferGroup(
                                    merchantId = acc.merchantId,
                                    merchantName = acc.merchantName,
                                    merchantPhone = acc.merchantPhone,
                                    offerCount = acc.offerCount + g.offerCount,
                                    employeeOffers = acc.employeeOffers + g.employeeOffers
                                )
                            }
                        }

                    _uiState.value = _uiState.value.copy(
                        isListLoading = false,
                        detail = newData.copy(merchantOffers = mergedOffers),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isListLoading = false, error = response.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isListLoading = false, error = e.message)
            }
        }
    }
}

/**
 * UI 状态
 */
data class SubstituteProductUiState(
    val isLoading: Boolean = true,
    val isListLoading: Boolean = false,
    val substituteProduct: SubstituteProduct? = null,
    val detail: SubstituteProductDetail? = null,
    val error: String? = null,
    val selectedFactoryNo: String = "",
    val offerType: String = "offer",
    val sortBy: String = "comprehensive",
    val page: Int = 1,
    val activeFilters: Set<String> = emptySet(),
    val category: String = "牛",
    val priceMin: String? = null,
    val priceMax: String? = null,
    val goodsTypes: Set<String> = emptySet(),
    val feedingTypes: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val isFamousMerchant: Boolean = false,
    val selectedMerchants: Set<Long> = emptySet(),
    val regions: Set<String> = emptySet(),
    val merchantOptions: List<MerchantOption> = emptyList(),
    val regionOptions: List<String> = emptyList()
)
