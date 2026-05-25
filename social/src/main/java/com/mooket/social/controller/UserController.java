package com.mooket.social.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mooket.social.common.ApiResponse;
import com.mooket.social.common.JwtUtil;
import com.mooket.social.entity.BizSearchHistory;
import com.mooket.social.entity.DictUser;
import com.mooket.social.mapper.BizSearchHistoryMapper;
import com.mooket.social.mapper.DictUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User profile endpoints.
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final String AVATAR_DIR = "/tmp/mooket/avatar/";
    private static final String AVATAR_BASE_URL = "https://twms.malleeglobal.com/social";

    @Autowired
    private DictUserMapper dictUserMapper;

    @Autowired
    private BizSearchHistoryMapper bizSearchHistoryMapper;

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        JwtUtil.ValidationResult validation = JwtUtil.validateTokenDetailed(token);
        if (!validation.isValid()) {
            return ApiResponse.error(401, validation.getMessage());
        }

        String phone = JwtUtil.getPhone(token);
        DictUser user = findActiveUserByPhone(phone);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && avatarUrl.startsWith("/avatar/")) {
            avatarUrl = AVATAR_BASE_URL + avatarUrl;
        }

        data.put("avatarUrl", avatarUrl);
        data.put("nickname", user.getNickname());
        data.put("phone", user.getPhone());
        data.put("mooketId", user.getMooketId());
        data.put("mooketNo", user.getMooketNo());
        data.put("realNameStatus", user.getRealNameStatus());
        data.put("realName", user.getRealName());
        data.put("identityTags",
                user.getIdentityTags() != null && !user.getIdentityTags().isEmpty()
                        ? Arrays.asList(user.getIdentityTags().split(","))
                        : Arrays.asList());
        return ApiResponse.success(data);
    }

    @PostMapping("/profile/update")
    public ApiResponse<Map<String, String>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {

        String token = authHeader.replace("Bearer ", "");
        JwtUtil.ValidationResult validation = JwtUtil.validateTokenDetailed(token);
        if (!validation.isValid()) {
            return ApiResponse.error(401, validation.getMessage());
        }

        String phone = JwtUtil.getPhone(token);
        DictUser user = findActiveUserByPhone(phone);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        String nickname = (String) request.get("nickname");
        @SuppressWarnings("unchecked")
        java.util.List<String> identityTags = (java.util.List<String>) request.get("identityTags");

        if (nickname != null) {
            if (nickname.length() < 2 || nickname.length() > 20) {
                return ApiResponse.error(400, "昵称需为 2-20 个字符");
            }
            user.setNickname(nickname);
        }
        if (identityTags != null) {
            user.setIdentityTags(String.join(",", identityTags));
        }

        user.setUpdateTime(LocalDateTime.now());
        dictUserMapper.updateById(user);

        Map<String, String> data = new HashMap<>();
        data.put("message", "更新成功");
        return ApiResponse.success(data);
    }

    @PostMapping("/avatar/upload")
    public ApiResponse<Map<String, String>> uploadAvatar(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {

        String token = authHeader.replace("Bearer ", "");
        JwtUtil.ValidationResult validation = JwtUtil.validateTokenDetailed(token);
        if (!validation.isValid()) {
            return ApiResponse.error(401, validation.getMessage());
        }

        Long userId = JwtUtil.getUserId(token);
        File dir = new File(AVATAR_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filename = userId + "_" + System.currentTimeMillis() + ".jpg";
        File destFile = new File(AVATAR_DIR, filename);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            return ApiResponse.error(500, "上传失败");
        }

        String avatarUrlPath = "/avatar/" + filename;
        DictUser user = dictUserMapper.selectById(userId);
        if (user != null) {
            user.setAvatarUrl(avatarUrlPath);
            user.setUpdateTime(LocalDateTime.now());
            dictUserMapper.updateById(user);
        }

        Map<String, String> data = new HashMap<>();
        data.put("avatarUrl", avatarUrlPath);
        data.put("message", "上传成功");
        return ApiResponse.success(data);
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        JwtUtil.ValidationResult validation = JwtUtil.validateTokenDetailed(token);
        if (!validation.isValid()) {
            return ApiResponse.error(401, validation.getMessage());
        }

        JwtUtil.invalidateSession(JwtUtil.getUserId(token));

        Map<String, String> data = new HashMap<>();
        data.put("message", "退出成功");
        return ApiResponse.success(data);
    }

    @GetMapping("/app/version")
    public ApiResponse<Map<String, Object>> getAppVersion() {
        Map<String, Object> data = new HashMap<>();
        data.put("version", "1.0");
        data.put("hasUpdate", false);
        return ApiResponse.success(data);
    }

    @PostMapping("/cancel-account")
    public ApiResponse<Map<String, String>> cancelAccount(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        JwtUtil.ValidationResult validation = JwtUtil.validateTokenDetailed(token);
        if (!validation.isValid()) {
            return ApiResponse.error(401, validation.getMessage());
        }

        String phone = JwtUtil.getPhone(token);
        DictUser user = findActiveUserByPhone(phone);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        user.setCancellationStatus("cancelled");
        user.setUpdateTime(LocalDateTime.now());
        dictUserMapper.updateById(user);
        JwtUtil.invalidateSession(user.getUserId());

        if (bizSearchHistoryMapper != null) {
            bizSearchHistoryMapper.delete(new QueryWrapper<BizSearchHistory>().eq("user_id", user.getUserId()));
        }

        Map<String, String> data = new HashMap<>();
        data.put("message", "注销成功");
        return ApiResponse.success(data);
    }

    private DictUser findActiveUserByPhone(String phone) {
        return dictUserMapper.selectOne(
                new QueryWrapper<DictUser>()
                        .eq("phone", phone)
                        .eq("cancellation_status", "active")
        );
    }
}
