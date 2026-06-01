/**
 * @file LocalFileProxyPathTest
 * @project pipker-do
 * @module 共享存储 / 本地代理路径测试
 * @description 验证本地文件代理路径从 public-base-url 中正确提取和校验。
 * @logic 1. 生成 Spring MVC 代理匹配表达式；2. 无 path 时使用 /static/files；3. 拒绝非法 URL。
 * @dependencies JUnit 5, LocalFileProxyPath
 * @index_tags 本地代理路径测试, public-base-url, /static/files
 * @author holic512
 */
package org.example.backend.util.file;

import org.junit.jupiter.api.Test;
import org.example.backend.shared.storage.driver.local.LocalFileProxyPath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalFileProxyPathTest {

    @Test
    void shouldBuildProxyPatternFromPublicBaseUrl() {
        assertEquals("/files/**",
                LocalFileProxyPath.toHandlerPattern("http://localhost:8080/files"));
        assertEquals("/static/files/**",
                LocalFileProxyPath.toHandlerPattern("https://cdn.example.com/static/files/"));
    }

    @Test
    void shouldFallbackToDefaultStaticFilesPatternWhenUrlHasNoPath() {
        assertEquals("/static/files/**",
                LocalFileProxyPath.toHandlerPattern("https://cdn.example.com"));
    }

    @Test
    void shouldRejectInvalidConfig() {
        assertThrows(IllegalArgumentException.class,
                () -> LocalFileProxyPath.toHandlerPattern("://bad-url"));
        assertThrows(IllegalStateException.class,
                () -> LocalFileProxyPath.toHandlerPattern(" "));
    }
}
