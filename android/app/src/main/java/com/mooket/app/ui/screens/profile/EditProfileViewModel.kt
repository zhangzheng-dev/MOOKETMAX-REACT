package com.mooket.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 编辑资料 UI 状态
 */
data class EditProfileUiState(
    val isLoading: Boolean = false,
    val avatarUrl: String? = null,
    val nickname: String = "",
    val realNameStatus: String? = null,  // approved / pending / rejected / null
    val realName: String? = null,
    val phone: String? = null,
    val identityTags: List<String> = emptyList(),
    val error: String? = null,
    val saveSuccess: Boolean = false
)

/**
 * 编辑资料 ViewModel
 */
class EditProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * 加载用户资料
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getUserProfile()
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            avatarUrl = profile.avatarUrl,
                            nickname = profile.nickname ?: "",
                            realNameStatus = profile.realNameStatus,
                            realName = profile.realName,
                            phone = profile.phone,
                            identityTags = profile.identityTags ?: emptyList()
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * 更新昵称
     */
    fun updateNickname(nickname: String) {
        _uiState.update { it.copy(nickname = nickname) }
    }

    /**
     * 更新行业身份
     */
    fun updateIdentityTags(tags: List<String>) {
        _uiState.update { it.copy(identityTags = tags) }
    }

    /**
     * 保存资料
     */
    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, saveSuccess = false) }

            val state = _uiState.value
            repository.updateProfile(
                nickname = state.nickname.ifEmpty { null },
                realName = null  // 实名认证不允许自行修改
            )
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
