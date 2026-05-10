package com.mooket.app.data.repository

import com.mooket.app.data.api.ApiService
import com.mooket.app.data.model.AppVersion
import com.mooket.app.data.model.UpdateProfileRequest
import com.mooket.app.data.model.UserProfile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.content.Context
import android.net.Uri

/**
 * 用户资料仓库
 */
class ProfileRepository(private val apiService: ApiService) {

    /**
     * 获取用户资料
     */
    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val response = apiService.getUserProfile()
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新用户资料
     */
    suspend fun updateProfile(nickname: String?, realName: String?, identityTags: List<String>? = null): Result<Unit> {
        return try {
            val response = apiService.updateProfile(UpdateProfileRequest(nickname, realName, identityTags))
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 上传头像
     */
    suspend fun uploadAvatar(file: File): Result<String> {
        return try {
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val response = apiService.uploadAvatar(part)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data.avatarUrl)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 上传头像（通过 URI 流式上传，解决相册读取权限问题）
     */
    suspend fun uploadAvatarUri(context: Context, uri: Uri, filename: String): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("无法读取图片"))

            val mediaType = "image/*".toMediaTypeOrNull()
            val requestBody = inputStream.readBytes().toRequestBody(mediaType)
            inputStream.close()

            val response = apiService.uploadAvatar(
                MultipartBody.Part.createFormData("file", filename, requestBody)
            )
            if (response.code == 200 && response.data != null) {
                Result.success(response.data.avatarUrl)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 登出
     */
    suspend fun logout(): Result<Unit> {
        return try {
            val response = apiService.logout()
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取App版本信息
     */
    suspend fun getAppVersion(): Result<AppVersion> {
        return try {
            val response = apiService.getAppVersion()
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 注销账号
     */
    suspend fun cancelAccount(): Result<Unit> {
        return try {
            val response = apiService.cancelAccount()
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}