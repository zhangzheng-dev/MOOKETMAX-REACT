package com.mooket.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.model.HomeCardItem
import com.mooket.app.data.repository.MooketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 首页卡片 ViewModel
 */
class HomeCardsViewModel : ViewModel() {

    private val repository = MooketRepository()

    private val _uiState = MutableStateFlow(HomeCardsUiState())
    val uiState: StateFlow<HomeCardsUiState> = _uiState.asStateFlow()

    init {
        loadRecentSearchCards()
        loadSelfSelectCards()
    }

    /**
     * 加载最近搜索的卡片数据
     */
    fun loadRecentSearchCards() {
        val category = _uiState.value.selectedCategory
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getRecentSearchCards(category)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        recentSearchCards = response.cards,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        recentSearchCards = emptyList(),
                        isLoading = false
                    )
                }
        }
    }

    /**
     * 加载自选搜索的卡片数据
     */
    fun loadSelfSelectCards() {
        val category = _uiState.value.selectedCategory
        viewModelScope.launch {
            repository.getSelfSelectCards(category)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(selfSelectCards = response.cards)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(selfSelectCards = emptyList())
                }
        }
    }

    /**
     * 加载卡片数据（原逻辑保留）
     */
    fun loadData() {
        loadRecentSearchCards()
        loadSelfSelectCards()
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
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        when (tab) {
            0 -> loadSelfSelectCards()
            1 -> loadRecentSearchCards()
        }
    }

    /**
     * 将历史记录移动到自选
     */
    fun moveToSelfSelect(historyId: Long) {
        // 乐观更新：立即从最近搜索移除
        _uiState.value = _uiState.value.copy(
            recentSearchCards = _uiState.value.recentSearchCards.filter { it.historyId != historyId }
        )
        viewModelScope.launch {
            repository.moveToSelfSelect(historyId)
                .onFailure {
                    loadRecentSearchCards()
                    loadSelfSelectCards()
                }
        }
    }

    /**
     * 删除历史搜索记录
     */
    fun deleteSearchHistory(historyId: Long) {
        // 乐观更新：立即从列表移除
        _uiState.value = _uiState.value.copy(
            recentSearchCards = _uiState.value.recentSearchCards.filter { it.historyId != historyId }
        )
        viewModelScope.launch {
            repository.deleteSearchHistory(historyId)
                .onFailure {
                    loadRecentSearchCards()
                }
        }
    }

    /**
     * 取消自选（从自选列表移除，卡片回到历史搜索列表）
     */
    fun cancelSelfSelect(historyId: Long) {
        // 乐观更新：立即从自选列表移除
        _uiState.value = _uiState.value.copy(
            selfSelectCards = _uiState.value.selfSelectCards.filter { it.historyId != historyId }
        )
        viewModelScope.launch {
            repository.cancelSelfSelect(historyId)
                .onFailure {
                    loadRecentSearchCards()
                    loadSelfSelectCards()
                }
        }
    }
}

/**
 * 首页卡片 UI 状态
 */
data class HomeCardsUiState(
    val isLoading: Boolean = false,
    val selectedCategory: String = "牛",
    val selectedTab: Int = 0, // 0=自选数据, 1=历史搜索数据
    val recentSearchCards: List<HomeCardItem> = emptyList(),
    val selfSelectCards: List<HomeCardItem> = emptyList()
)
