package com.mooket.app.ui.screens.datacomparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.model.FactoryPriceComparison
import com.mooket.app.data.repository.MooketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 数据对比 ViewModel
 */
class DataComparisonViewModel : ViewModel() {

    private val repository = MooketRepository()

    private val _uiState = MutableStateFlow(DataComparisonUiState())
    val uiState: StateFlow<DataComparisonUiState> = _uiState.asStateFlow()

    private var allFactories: List<String> = emptyList()

    /**
     * 初始化数据（默认选中前4个厂号并加载数据）
     */
    fun initialize(country: String, factoryNos: List<String>, productName: String, category: String, excludeFactoryNo: String? = null) {
        allFactories = factoryNos
        // 默认选中前4个厂号，但排除被排除的原始厂号
        val defaultSelected = factoryNos.filterNot { it == excludeFactoryNo }.take(4).toSet()
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = null,
            country = country,
            productName = productName,
            category = category,
            allFactories = factoryNos,
            selectedFactories = defaultSelected,
            comparisonData = null
        )
        // 加载数据
        if (defaultSelected.isNotEmpty()) {
            loadComparisonData(defaultSelected.toList())
        }
    }

    /**
     * 切换厂号选中状态
     */
    fun toggleFactory(factoryNo: String, excludeFactoryNo: String?) {
        val current = _uiState.value.selectedFactories
        val updated = if (current.contains(factoryNo)) {
            current - factoryNo
        } else {
            // 计算排除后的有效选中数量
            val effectiveSize = current.count { it != excludeFactoryNo }
            if (effectiveSize >= 6) return // 最多6条线
            current + factoryNo
        }
        _uiState.value = _uiState.value.copy(selectedFactories = updated)

        // 如果有选中厂号，立即加载数据
        if (updated.isNotEmpty()) {
            loadComparisonData(updated.toList())
        } else {
            _uiState.value = _uiState.value.copy(comparisonData = null)
        }
    }

    /**
     * 加载对比数据
     */
    private fun loadComparisonData(factoryNos: List<String>) {
        _uiState.value = _uiState.value.copy(isContentLoading = true, error = null)

        viewModelScope.launch {
            val result = repository.getFactoryPriceComparison(
                country = _uiState.value.country,
                factoryNos = factoryNos,
                productName = _uiState.value.productName,
                category = _uiState.value.category,
                offerType = "报盘",
                days = 30
            )

            result.onSuccess { data ->
                // 按当天日均价排序：有价格的排在前面
                val sortedFactories = if (_uiState.value.selectedDateIndex < data.factories.firstOrNull()?.trend?.size ?: 0) {
                    val todayIdx = data.factories.firstOrNull()?.trend?.size?.minus(1) ?: 0
                    _uiState.value.allFactories.sortedByDescending { factoryNo ->
                        data.factories.find { it.factoryNo == factoryNo }?.trend?.getOrNull(todayIdx)?.avgPrice != null
                    }
                } else {
                    _uiState.value.allFactories
                }
                _uiState.value = _uiState.value.copy(
                    isContentLoading = false,
                    comparisonData = data,
                    selectedDateIndex = data.factories.firstOrNull()?.trend?.size?.minus(1) ?: 0,
                    allFactories = sortedFactories,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isContentLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    /**
     * 选择日期索引（用于 tooltip 显示）
     */
    fun selectDateIndex(index: Int) {
        _uiState.value = _uiState.value.copy(selectedDateIndex = index)
        // 重新排序：把当天有日均价的厂号排在前面
        resortFactoriesByPrice(index)
    }

    private fun resortFactoriesByPrice(dateIndex: Int) {
        val data = _uiState.value.comparisonData ?: return
        val sortedAll = _uiState.value.allFactories.sortedByDescending { factoryNo ->
            data.factories.find { it.factoryNo == factoryNo }?.trend?.getOrNull(dateIndex)?.avgPrice != null
        }
        _uiState.value = _uiState.value.copy(allFactories = sortedAll)
    }
}

/**
 * UI 状态
 */
data class DataComparisonUiState(
    val isLoading: Boolean = false,
    val isContentLoading: Boolean = false,
    val comparisonData: FactoryPriceComparison? = null,
    val selectedDateIndex: Int = 0,
    val selectedFactories: Set<String> = emptySet(),
    val allFactories: List<String> = emptyList(),
    val error: String? = null,
    val country: String = "",
    val productName: String = "",
    val category: String = "牛"
)
