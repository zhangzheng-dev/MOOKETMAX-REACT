package com.mooket.app.data.repository

import com.mooket.app.data.api.ApiService
import com.mooket.app.data.model.AppVersion
import com.mooket.app.data.model.UpdateProfileRequest
import com.mooket.app.data.model.UserProfile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

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
    suspend fun updateProfile(nickname: String?, realName: String?): Result<Unit> {
        return try {
            val response = apiService.updateProfile(UpdateProfileRequest(nickname, realName))
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
            val part = MultipartBody.Part.createFormData("avatar", file.name, requestBody)
            val response = apiService.uploadAvatar(part)
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