/**
 * @file LocalFileProxyPath
 * @project pipker-do
 * @module 共享存储 / 本地文件代理路径
 * @description 从 storage.local.public-base-url 提取本地代理路径和 Spring MVC 匹配规则。
 * @logic 1. 解析 URL path；2. 规范化尾部斜杠；3. 生成 /** 代理匹配表达式。
 * @dependencies JDK URI
 * @index_tags 本地代理, public-base-url, 路径解析, /static/files
 * @author holic512
 */
package org.example.backend.shared.storage.driver.local;

import java.net.URI;
import java.net.URISyntaxException;

public final class LocalFileProxyPath {

    private LocalFileProxyPath() {
    }

    public static String toProxyPath(String publicBaseUrl) {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            throw new IllegalStateException("请在配置中设置 storage.local.public-base-url");
        }
        try {
            URI uri = new URI(publicBaseUrl);
            String path = uri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                path = "/static/files";
            }
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            while (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }
            return path;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("storage.local.public-base-url 非法: " + publicBaseUrl, e);
        }
    }

    public static String toHandlerPattern(String publicBaseUrl) {
        return toProxyPath(publicBaseUrl) + "/**";
    }
}
