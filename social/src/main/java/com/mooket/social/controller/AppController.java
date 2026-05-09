package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * APP级别 Controller
 */
@RestController
@RequestMapping("/api/v1/app")
public class AppController {

    /**
     * 获取APP版本
     */
    @GetMapping("/version")
    public ApiResponse<Map<String, Object>> getAppVersion() {
        Map<String, Object> data = new HashMap<>();
        data.put("version", "1.0");
        data.put("hasUpdate", false);
        return ApiResponse.success(data);
    }
}