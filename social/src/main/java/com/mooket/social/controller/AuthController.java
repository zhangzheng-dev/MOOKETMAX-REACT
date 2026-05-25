package com.mooket.social.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mooket.social.common.ApiResponse;
import com.mooket.social.common.JwtUtil;
import com.mooket.social.entity.DictUser;
import com.mooket.social.entity.uac.UacUser;
import com.mooket.social.gateway.GatewayOAuthClient;
import com.mooket.social.mapper.DictUserMapper;
import com.mooket.social.uac.mapper.UacUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private DictUserMapper dictUserMapper;

    @Autowired
    private UacUserMapper uacUserMapper;

    @Autowired
    private GatewayOAuthClient gatewayOAuthClient;

    private final Map<String, Long> smsCooldownStore = new ConcurrentHashMap<>();

    @PostMapping("/send-code")
    public ApiResponse<Map<String, Object>> sendCode(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {

        String phone = request.get("phone");
        if (phone == null || phone.length() != 11) {
            return ApiResponse.error(400, "手机号格式错误");
        }

        Long cooldownEnd = smsCooldownStore.get(phone);
        if (cooldownEnd != null && System.currentTimeMillis() < cooldownEnd) {
            return ApiResponse.error(400, "请稍后再试");
        }

        try {
            boolean isDisabled = gatewayOAuthClient.isUserDisabled(phone);
            if (isDisabled) {
                return ApiResponse.error(403, "账号已被禁用");
            }

            GatewayOAuthClient.MobileRegisterCheckResult registerCheck =
                    gatewayOAuthClient.checkMobileRegister(phone);

            String realDeviceId = deviceId != null ? deviceId : "server";
            gatewayOAuthClient.sendSmsCode(phone, realDeviceId);

            smsCooldownStore.put(phone, System.currentTimeMillis() + 60000);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "验证码已发送");
            data.put("isRegistered", registerCheck.isRegistered);
            data.put("clientId", String.valueOf(System.currentTimeMillis()));
            return ApiResponse.success(data);
        } catch (GatewayOAuthClient.GatewayException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String code = request.get("code");
        String clientId = request.get("clientId");
        String deviceId = request.get("deviceId");

        if (phone == null || code == null) {
            return ApiResponse.error(400, "参数不完整");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return ApiResponse.error(400, "手机号格式错误");
        }
        if (!code.matches("^\\d{4,6}$")) {
            return ApiResponse.error(400, "请输入正确的验证码");
        }

        try {
            GatewayOAuthClient.MobileRegisterCheckResult registerCheck =
                    gatewayOAuthClient.checkMobileRegister(phone);

            String realDeviceId = deviceId != null ? deviceId : "server";
            GatewayOAuthClient.GatewayTokenResult tokenResult =
                    gatewayOAuthClient.loginWithSmsCode(phone, code, clientId, realDeviceId);

            DictUser user = syncUserFromUac(phone, tokenResult.userId);
            String jwtToken = JwtUtil.generateToken(user.getUserId(), phone);
            smsCooldownStore.remove(phone);

            Map<String, Object> data = new HashMap<>();
            data.put("token", jwtToken);
            data.put("isNewUser", !registerCheck.isRegistered);
            data.put("userId", user.getUserId());
            data.put("phone", user.getPhone());
            data.put("nickname", user.getNickname());
            data.put("mooketId", user.getMooketId());
            data.put("gatewayAccessToken", tokenResult.accessToken);
            data.put("gatewayUserId", tokenResult.userId);
            return ApiResponse.success(data);
        } catch (GatewayOAuthClient.GatewayException e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {

        String token = authHeader.replace("Bearer ", "");
        JwtUtil.ValidationResult validation = JwtUtil.validateTokenDetailed(token);
        if (!validation.isValid()) {
            return ApiResponse.error(401, validation.getMessage());
        }

        String phone = JwtUtil.getPhone(token);
        String nickname = (String) request.get("nickname");
        @SuppressWarnings("unchecked")
        List<String> identityTags = (List<String>) request.get("identityTags");
        String gatewayAccessToken = (String) request.get("gatewayAccessToken");
        String code = (String) request.get("code");
        String clientId = (String) request.get("clientId");
        String deviceId = (String) request.get("deviceId");

        if (nickname == null || nickname.length() < 2 || nickname.length() > 20) {
            return ApiResponse.error(400, "昵称需为 2-20 个字符");
        }
        if (identityTags == null || identityTags.isEmpty()) {
            return ApiResponse.error(400, "请至少选择一个身份标签");
        }
        if (gatewayAccessToken == null || gatewayAccessToken.isEmpty()) {
            return ApiResponse.error(400, "缺少 gatewayToken");
        }
        if (code == null) {
            return ApiResponse.error(400, "参数不完整");
        }

        try {
            gatewayOAuthClient.finishBootstrap(gatewayAccessToken, nickname, identityTags);

            String realDeviceId = deviceId != null ? deviceId : "server";
            GatewayOAuthClient.GatewayTokenResult tokenResult =
                    gatewayOAuthClient.loginWithSmsCode(phone, code, clientId, realDeviceId);

            DictUser user = dictUserMapper.selectOne(
                    new QueryWrapper<DictUser>()
                            .eq("phone", phone)
                            .eq("cancellation_status", "active")
            );
            if (user == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            user.setNickname(nickname);
            user.setIdentityTags(String.join(",", identityTags));
            user.setUpdateTime(LocalDateTime.now());
            dictUserMapper.updateById(user);

            String jwtToken = JwtUtil.generateToken(user.getUserId(), phone);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "注册成功");
            data.put("token", jwtToken);
            data.put("userId", user.getUserId());
            data.put("phone", user.getPhone());
            data.put("nickname", nickname);
            data.put("mooketId", user.getMooketId());
            data.put("gatewayAccessToken", tokenResult.accessToken);
            data.put("gatewayUserId", tokenResult.userId);
            return ApiResponse.success(data);
        } catch (GatewayOAuthClient.GatewayException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/userinfo")
    public ApiResponse<DictUser> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        JwtUtil.ValidationResult validation = JwtUtil.validateTokenDetailed(token);
        if (!validation.isValid()) {
            return ApiResponse.error(401, validation.getMessage());
        }

        String phone = JwtUtil.getPhone(token);
        DictUser user = dictUserMapper.selectOne(
                new QueryWrapper<DictUser>()
                        .eq("phone", phone)
                        .eq("cancellation_status", "active")
        );
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        return ApiResponse.success(user);
    }

    private DictUser syncUserFromUac(String phone, String gatewayUserId) {
        DictUser user = dictUserMapper.selectOne(
                new QueryWrapper<DictUser>()
                        .eq("phone", phone)
                        .eq("cancellation_status", "active")
        );

        boolean isNewUser = user == null;
        if (isNewUser) {
            user = new DictUser();
            user.setPhone(phone);
            user.setCreateTime(LocalDateTime.now());
        }

        UacUser uacUser = uacUserMapper.selectByPhone(phone);
        if (uacUser != null) {
            user.setMooketNo(uacUser.getUserMujiNo());
            user.setMooketId(String.valueOf(uacUser.getId()));
            user.setNickname(uacUser.getNickName());
            user.setRealName(uacUser.getUserName());
            String faceUrl = uacUser.getFaceUrl();
            user.setAvatarUrl(faceUrl != null && !faceUrl.isBlank() ? faceUrl : uacUser.getAnonymousFaceUrl());
            if (uacUser.getIsIdentification() != null) {
                user.setRealNameStatus(uacUser.getIsIdentification() == 1 ? "verified" : "pending");
            }
        }

        user.setUpdateTime(LocalDateTime.now());

        if (isNewUser) {
            dictUserMapper.insert(user);
        } else {
            dictUserMapper.updateById(user);
        }

        return user;
    }
}
