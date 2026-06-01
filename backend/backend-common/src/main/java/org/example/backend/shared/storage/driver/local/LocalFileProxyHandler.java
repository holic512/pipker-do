/**
 * @file LocalFileProxyHandler
 * @project pipker-do
 * @module 共享存储 / 本地文件代理处理器
 * @description 在本地存储模式下代理输出系统自管文件，替代直接静态资源映射。
 * @logic 1. 根据 public-base-url 剥离代理前缀；2. 校验相对路径并解析本地文件；3. 流式写出文件内容。
 * @dependencies LocalFileStorage, LocalStorageProperties, StorageKey
 * @index_tags 本地文件代理, pipker#, 文件下载, 路径安全
 * @author holic512
 */
package org.example.backend.shared.storage.driver.local;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.shared.storage.config.LocalStorageProperties;
import org.example.backend.shared.storage.core.StorageKey;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileProxyHandler implements HttpRequestHandler {

    private final LocalStorageProperties properties;
    private final LocalFileStorage localFileStorage;

    public LocalFileProxyHandler(LocalStorageProperties properties, LocalFileStorage localFileStorage) {
        this.properties = properties;
        this.localFileStorage = localFileStorage;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String relativePath = extractRelativePath(request);
        if (relativePath == null || relativePath.isBlank()) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        StorageKey storageKey;
        try {
            storageKey = StorageKey.parseRelativeStoragePath(relativePath);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpStatus.BAD_REQUEST.value());
            return;
        }

        Path filePath = localFileStorage.resolvePath(storageKey.asString());
        if (filePath == null || !Files.isRegularFile(filePath)) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType != null && !contentType.isBlank()) {
            response.setContentType(contentType);
        }
        response.setContentLengthLong(Files.size(filePath));
        Files.copy(filePath, response.getOutputStream());
    }

    private String extractRelativePath(HttpServletRequest request) {
        String proxyPath = LocalFileProxyPath.toProxyPath(properties.getPublicBaseUrl());
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && requestPath.startsWith(contextPath)) {
            requestPath = requestPath.substring(contextPath.length());
        }
        if (!requestPath.equals(proxyPath) && !requestPath.startsWith(proxyPath + "/")) {
            return null;
        }
        String relativePath = requestPath.substring(proxyPath.length());
        while (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return UriUtils.decode(relativePath, StandardCharsets.UTF_8);
    }
}
