package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * App-level endpoints.
 */
@RestController
@RequestMapping("/api/v1/app")
public class AppController {

    private static final String CURRENT_VERSION = "1.0.2";
    private static final int CURRENT_VERSION_CODE = 4;
    private static final String UPDATE_URL =
            "https://twms.malleeglobal.com/social/api/v1/app/download/apk";
    private static final String UPDATE_CONTENT = String.join("\n",
            "1. 修复产品、品牌、国家、厂号等详情页商家/厂号列表横向滑动问题。",
            "2. 优化详情页卡片点击与横向滚动手势冲突，浏览操作更顺畅。",
            "3. 修复已知问题并优化整体稳定性。");

    @GetMapping("/version")
    public ApiResponse<Map<String, Object>> getAppVersion(
            @RequestParam(value = "versionCode", required = false) Integer clientVersionCode,
            @RequestParam(value = "version", required = false) String clientVersion
    ) {
        Map<String, Object> data = new HashMap<>();
        boolean hasUpdate = clientVersionCode == null
                ? clientVersion == null || !CURRENT_VERSION.equals(clientVersion)
                : CURRENT_VERSION_CODE > clientVersionCode;
        data.put("version", CURRENT_VERSION);
        data.put("versionCode", CURRENT_VERSION_CODE);
        data.put("hasUpdate", hasUpdate);
        data.put("updateUrl", UPDATE_URL);
        data.put("updateContent", UPDATE_CONTENT);
        return ApiResponse.success(data);
    }

    @GetMapping("/download/apk")
    public ResponseEntity<Resource> downloadApk() {
        File file = new File("/tmp/mooket.apk");
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mooket-max-1.0.2.apk")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
