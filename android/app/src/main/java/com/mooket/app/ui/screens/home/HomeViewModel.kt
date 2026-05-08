package com.mooket.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.model.HomeCardItem
import com.mooket.app.data.model.HomeStatData
import com.mooket.app.data.model.HotSearchItem
import com.mooket.app.data.repository.MooketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 首页 ViewModel
 */
class HomeViewModel : ViewModel() {

    private val repository = MooketRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * 加载首页数据
     */
    fun loadData() {
        val category = _uiState.value.selectedCategory
        // 重置 tab 自动切换标记（换品类时重新判断）
        _uiState.value = _uiState.value.copy(tabsInitialized = false, tabsPendingCount = 2)

        loadHotSearch(category)
        loadHomeStat(category)
        loadRecentSearchCards(category)
        loadSelfSelectCards(category)
    }

    /**
     * 切换品类
     */
    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadData()
    }

    /**
     * 切换 Tab（0=自选数据, 1=历史搜索数据）
     * 状态提升到 ViewModel，导航返回后不丢失
     */
    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, tabsInitialized = true)
    }

    /**
     * 加载热门搜索
     */
    private fun loadHotSearch(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getHotSearchRecommendations(category)
                .onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        hotSearchItems = items,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        hotSearchItems = emptyList(),
                        isLoading = false
                    )
                }
        }
    }

    /**
     * 加载首页统计
     */
    private fun loadHomeStat(category: String) {
        viewModelScope.launch {
            repository.getHomeStatData(category)
                .onSuccess { stat ->
                    _uiState.value = _uiState.value.copy(homeStatData = stat)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        homeStatData = HomeStatData("--", "--", "--", "加载失败")
                    )
                }
        }
    }

    /**
     * 加载最近搜索的卡片数据
     */
    private fun loadRecentSearchCards(category: String) {
        viewModelScope.launch {
            repository.getRecentSearchCards(category)
                .onSuccess { cards ->
                    _uiState.value = _uiState.value.copy(recentSearchCards = cards.cards)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(recentSearchCards = emptyList())
                }
            tabsLoadDone()
        }
    }

    /**
     * 加载自选搜索的卡片数据
     */
    private fun loadSelfSelectCards(category: String) {
        viewModelScope.launch {
            repository.getSelfSelectCards(category)
                .onSuccess { cards ->
                    _uiState.value = _uiState.value.copy(selfSelectCards = cards.cards)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(selfSelectCards = emptyList())
                }
            tabsLoadDone()
        }
    }

    /**
     * 两个 tab 数据请求都完成后的自动 tab 切换逻辑
     */
    private fun tabsLoadDone() {
        val remaining = _uiState.value.tabsPendingCount - 1
        _uiState.value = _uiState.value.copy(tabsPendingCount = remaining)
        if (remaining == 0 && !_uiState.value.tabsInitialized) {
            // 自选数据为空 → 自动切到历史搜索 tab
            if (_uiState.value.selfSelectCards.isEmpty()) {
                _uiState.value = _uiState.value.copy(selectedTab = 1)
            }
            _uiState.value = _uiState.value.copy(tabsInitialized = true)
        }
    }

    /**
     * 添加自选
     */
    fun addSelfSelect(searchWord: String, searchType: String) {
        viewModelScope.launch {
            repository.addSelfSelect(searchWord, searchType)
                .onSuccess {
                    loadSelfSelectCards(_uiState.value.selectedCategory)
                }
        }
    }

    /**
     * 取消自选
     */
    fun cancelSelfSelect(historyId: Long) {
        // 乐观更新：立即从列表移除，卡片瞬间消失
        _uiState.value = _uiState.value.copy(
            selfSelectCards = _uiState.value.selfSelectCards.filter { it.historyId != historyId }
        )
        viewModelScope.launch {
            repository.cancelSelfSelect(historyId)
                .onFailure {
                    // 失败时回滚，重新加载
                    loadSelfSelectCards(_uiState.value.selectedCategory)
                }
        }
    }

    /**
     * 刷新最近搜索卡片（切换Tab或返回页面时调用）
     */
    fun refreshRecentSearchCards() {
        loadRecentSearchCards(_uiState.value.selectedCategory)
    }

    /**
     * 刷新自选卡片
     */
    fun refreshSelfSelectCards() {
        loadSelfSelectCards(_uiState.value.selectedCategory)
    }

    /**
     * 刷新首页统计数据（用于定时刷新）
     */
    fun refreshHomeStat() {
        loadHomeStat(_uiState.value.selectedCategory)
    }

    /**
     * 删除最近搜索记录
     */
    fun deleteRecentSearch(historyId: Long) {
        // 乐观更新：立即从列表移除
        _uiState.value = _uiState.value.copy(
            recentSearchCards = _uiState.value.recentSearchCards.filter { it.historyId != historyId }
        )
        viewModelScope.launch {
            repository.deleteSearchHistory(historyId)
                .onFailure {
                    loadRecentSearchCards(_uiState.value.selectedCategory)
                }
        }
    }

    /**
     * 将最近搜索移动到自选
     */
    fun moveToSelfSelect(historyId: Long) {
        // 乐观更新：立即从最近搜索移除
        _uiState.value = _uiState.value.copy(
            recentSearchCards = _uiState.value.recentSearchCards.filter { it.historyId != historyId }
        )
        viewModelScope.launch {
            repository.moveToSelfSelect(historyId)
                .onFailure {
                    loadRecentSearchCards(_uiState.value.selectedCategory)
                    loadSelfSelectCards(_uiState.value.selectedCategory)
                }
        }
    }
}

/**
 * 首页 UI 状态
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val selectedCategory: String = "牛",
    val selectedTab: Int = 0, // 0=自选数据, 1=历史搜索数据
    val hotSearchItems: List<HotSearchItem> = emptyList(),
    val homeStatData: HomeStatData? = null,
    val recentSearchCards: List<HomeCardItem> = emptyList(),
    val selfSelectCards: List<HomeCardItem> = emptyList(),
    val tabsInitialized: Boolean = false, // 首次加载时自动选tab
    val tabsPendingCount: Int = 0 // 待完成的tab数据请求计数
)
