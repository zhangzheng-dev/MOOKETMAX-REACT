package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * App-level endpoints.
 */
@RestController
@RequestMapping("/api/v1/app")
public class AppController {

    private static final String FALLBACK_VERSION = "1.0.3";
    private static final int FALLBACK_VERSION_CODE = 5;
    private static final String UPDATE_URL_BASE =
            "https://twms.malleeglobal.com/social/api/v1/app/download/apk";
    private static final String DEFAULT_UPDATE_CONTENT =
            "1. 发现新版本\n2. 点击更新即可下载安装\n3. 如安装失败，请退出后重试";
    private static final Path HOT_UPDATE_DIR = Path.of("/tmp/mooket-hot-updates");
    private static final Path LEGACY_APK_PATH = Path.of("/tmp/mooket.apk");
    private static final Pattern APK_NAME_PATTERN =
            Pattern.compile("^mooket-max-(.+)-v(\\d+)\\.apk$");

    @GetMapping("/version")
    public ApiResponse<Map<String, Object>> getAppVersion(
            @RequestParam(value = "versionCode", required = false) Integer clientVersionCode,
            @RequestParam(value = "version", required = false) String clientVersion
    ) {
        UpdatePackageInfo packageInfo = resolveUpdatePackage();
        Map<String, Object> data = new HashMap<>();
        boolean hasUpdate = clientVersionCode == null
                ? clientVersion == null || !packageInfo.version().equals(clientVersion)
                : packageInfo.versionCode() > clientVersionCode;
        data.put("version", packageInfo.version());
        data.put("versionCode", packageInfo.versionCode());
        data.put("hasUpdate", hasUpdate);
        data.put("updateUrl", buildVersionedUpdateUrl(packageInfo));
        data.put("updateContent", packageInfo.updateContent());
        return ApiResponse.success(data);
    }

    @GetMapping("/download/apk")
    public ResponseEntity<Resource> downloadApk() {
        UpdatePackageInfo packageInfo = resolveUpdatePackage();
        File file = packageInfo.file().toFile();
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().mustRevalidate())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + packageInfo.fileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private String buildVersionedUpdateUrl(UpdatePackageInfo packageInfo) {
        return UPDATE_URL_BASE
                + "?version="
                + packageInfo.version()
                + "&versionCode="
                + packageInfo.versionCode();
    }

    private UpdatePackageInfo resolveUpdatePackage() {
        return findLatestUploadedPackage().orElseGet(this::fallbackPackageInfo);
    }

    private Optional<UpdatePackageInfo> findLatestUploadedPackage() {
        if (!Files.isDirectory(HOT_UPDATE_DIR)) {
            return Optional.empty();
        }

        try (Stream<Path> stream = Files.list(HOT_UPDATE_DIR)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(this::parsePackageInfo)
                    .flatMap(Optional::stream)
                    .max(Comparator
                            .comparingInt(UpdatePackageInfo::versionCode)
                            .thenComparing(info -> info.file().toFile().lastModified()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<UpdatePackageInfo> parsePackageInfo(Path file) {
        String fileName = file.getFileName().toString();
        Matcher matcher = APK_NAME_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String version = matcher.group(1);
        int versionCode;
        try {
            versionCode = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        return Optional.of(new UpdatePackageInfo(
                version,
                versionCode,
                file,
                fileName,
                resolveUpdateContent(file)
        ));
    }

    private String resolveUpdateContent(Path apkPath) {
        String fileName = apkPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        Path siblingText = apkPath.resolveSibling(baseName + ".txt");
        if (Files.isRegularFile(siblingText)) {
            try {
                String content = Files.readString(siblingText, StandardCharsets.UTF_8)
                        .replace("\uFEFF", "")
                        .trim();
                if (!content.isEmpty()) {
                    return content;
                }
            } catch (IOException ignored) {
            }
        }
        return DEFAULT_UPDATE_CONTENT;
    }

    private UpdatePackageInfo fallbackPackageInfo() {
        return new UpdatePackageInfo(
                FALLBACK_VERSION,
                FALLBACK_VERSION_CODE,
                LEGACY_APK_PATH,
                "mooket-max-" + FALLBACK_VERSION + "-v" + FALLBACK_VERSION_CODE + ".apk",
                DEFAULT_UPDATE_CONTENT
        );
    }

    private record UpdatePackageInfo(
            String version,
            int versionCode,
            Path file,
            String fileName,
            String updateContent
    ) {
    }
}
