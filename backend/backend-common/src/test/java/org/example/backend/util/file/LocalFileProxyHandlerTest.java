/**
 * @file LocalFileProxyHandlerTest
 * @project pipker-do
 * @module 共享存储 / 本地文件代理测试
 * @description 验证本地文件代理能输出受管文件并拒绝非法路径。
 * @logic 1. 上传文件后按 public-base-url 路径代理读取；2. 编码后的目录穿越路径返回 400。
 * @dependencies JUnit 5, Spring MockMvc servlet, LocalFileProxyHandler
 * @index_tags 本地代理测试, 文件输出, 路径穿越, pipker#
 * @author holic512
 */
package org.example.backend.util.file;

import org.example.backend.shared.storage.config.ImageCompressionProperties;
import org.example.backend.shared.storage.config.LocalStorageProperties;
import org.example.backend.shared.storage.core.FileUploadProcessor;
import org.example.backend.shared.storage.core.LegacyStorageKeyResolver;
import org.example.backend.shared.storage.driver.local.LocalFileProxyHandler;
import org.example.backend.shared.storage.driver.local.LocalFileStorage;
import org.example.backend.shared.storage.driver.local.LocalStorageUrlResolver;
import org.example.backend.shared.storage.image.ImageCompressionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalFileProxyHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldProxyManagedLocalFile() throws Exception {
        LocalStorageProperties properties = properties();
        LocalFileStorage storage = newStorage(properties);
        String key = storage.save(textFile("a.txt", "hello"), "avatar");
        String requestPath = URI.create(storage.resolveUrl(key)).getPath();
        LocalFileProxyHandler handler = new LocalFileProxyHandler(properties, storage);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handleRequest(new MockHttpServletRequest("GET", requestPath), response);

        assertEquals(200, response.getStatus());
        assertEquals("hello", response.getContentAsString());
    }

    @Test
    void shouldRejectTraversalPath() throws Exception {
        LocalStorageProperties properties = properties();
        LocalFileProxyHandler handler = new LocalFileProxyHandler(properties, newStorage(properties));
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handleRequest(new MockHttpServletRequest("GET", "/static/files/avatar/%2e%2e/secret.txt"), response);

        assertEquals(400, response.getStatus());
    }

    private LocalStorageProperties properties() {
        LocalStorageProperties properties = new LocalStorageProperties();
        properties.setBasePath(tempDir.resolve("data").toString());
        properties.setTempDir(".tmp");
        properties.setPublicBaseUrl("http://localhost:8080/static/files");
        properties.setAllowedBizTypes(List.of("avatar"));
        return properties;
    }

    private LocalFileStorage newStorage(LocalStorageProperties properties) {
        LegacyStorageKeyResolver legacyStorageKeyResolver = new LegacyStorageKeyResolver();
        LocalStorageUrlResolver storageUrlResolver = new LocalStorageUrlResolver(properties, legacyStorageKeyResolver);
        ImageCompressionProperties imageCompressionProperties = new ImageCompressionProperties();
        imageCompressionProperties.setEnabled(false);
        FileUploadProcessor fileUploadProcessor = new FileUploadProcessor(
                properties,
                new ImageCompressionService(imageCompressionProperties)
        );
        return new LocalFileStorage(properties, legacyStorageKeyResolver, storageUrlResolver, fileUploadProcessor);
    }

    private MockMultipartFile textFile(String filename, String content) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
