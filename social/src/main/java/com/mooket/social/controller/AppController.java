package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * APP-level endpoints.
 */
@RestController
@RequestMapping("/api/v1/app")
public class AppController {

    private static final String CURRENT_VERSION = "1.0.0";
    private static final int CURRENT_VERSION_CODE = 2;
    private static final String UPDATE_URL =
            "https://twms.malleeglobal.com/social/api/v1/app/download/apk";
    private static final String UPDATE_CONTENT =
            "1. 优化性能，提升加载速度\n2. 修复已知问题\n3. 体验优化";

    @GetMapping("/version")
    public ApiResponse<Map<String, Object>> getAppVersion() {
        Map<String, Object> data = new HashMap<>();
        data.put("version", CURRENT_VERSION);
        data.put("versionCode", CURRENT_VERSION_CODE);
        data.put("hasUpdate", true);
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mooket.apk")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
