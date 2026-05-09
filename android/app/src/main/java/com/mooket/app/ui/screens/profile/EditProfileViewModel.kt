package com.mooket.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mooket.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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

    private var selectedImageUri: Uri? = null
    private var tempImageFile: File? = null

    init {
        // 延迟加载
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
     * 获取临时文件路径（用于拍照）
     */
    fun getTempImageFile(context: Context): File {
        tempImageFile = File.createTempFile(
            "avatar_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
        return tempImageFile!!
    }

    /**
     * 设置选中的图片 URI
     */
    fun setSelectedImageUri(uri: Uri) {
        selectedImageUri = uri
    }

    /**
     * 清除选中的图片
     */
    fun clearSelectedImage() {
        selectedImageUri = null
        tempImageFile?.delete()
        tempImageFile = null
    }

    /**
     * 上传头像
     */
    fun uploadAvatar(context: Context) {
        val uri = selectedImageUri ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // 将 URI 复制到临时文件
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile(
                    "upload_avatar_${System.currentTimeMillis()}",
                    ".jpg",
                    context.cacheDir
                )
                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                repository.uploadAvatar(tempFile)
                    .onSuccess { newAvatarUrl ->
                        _uiState.update { it.copy(isLoading = false, avatarUrl = newAvatarUrl) }
                        tempFile.delete()
                        clearSelectedImage()
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                        tempFile.delete()
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * 拍照后设置临时文件 URI
     */
    fun setTempFileUri(context: Context, file: File): Uri {
        tempImageFile = file
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
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
    fun saveProfile(nickname: String, identityTags: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, saveSuccess = false) }

            repository.updateProfile(
                nickname = nickname.ifEmpty { null },
                realName = null,  // 实名认证不允许自行修改
                identityTags = identityTags.takeIf { it.isNotEmpty() }
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
