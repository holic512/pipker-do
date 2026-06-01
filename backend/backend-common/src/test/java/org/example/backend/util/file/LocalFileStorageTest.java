/**
 * @file LocalFileStorageTest
 * @project pipker-do
 * @module 共享存储 / 本地文件存储测试
 * @description 验证本地文件存储的系统 key、去重、URL 解析、删除和旧 key 兼容行为。
 * @logic 1. 上传返回 pipker# key；2. 相同内容复用同一文件；3. legacy key 可解析到本地路径。
 * @dependencies JUnit 5, LocalFileStorage, FileUploadProcessor
 * @index_tags 本地存储测试, pipker#, 文件去重, legacy key
 * @author holic512
 */
package org.example.backend.util.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.example.backend.shared.storage.config.ImageCompressionProperties;
import org.example.backend.shared.storage.core.LegacyStorageKeyResolver;
import org.example.backend.shared.storage.driver.local.LocalFileStorage;
import org.example.backend.shared.storage.config.LocalStorageProperties;
import org.example.backend.shared.storage.core.FileUploadProcessor;
import org.example.backend.shared.storage.image.ImageCompressionService;
import org.example.backend.shared.storage.driver.local.LocalStorageUrlResolver;
import org.example.backend.shared.storage.core.StoredFileInfo;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateStableKeyAndReuseExistingFile() throws IOException {
        LocalFileStorage storage = newStorage(tempDir, List.of("avatar"));
        MockMultipartFile file1 = textFile("a.txt", "hello world");
        MockMultipartFile file2 = textFile("b.txt", "hello world");

        String key1 = storage.save(file1, "avatar");
        String key2 = storage.save(file2, "avatar");

        assertEquals(key1, key2);
        assertTrue(key1.startsWith("pipker#avatar/"));
        Path storedPath = storage.resolvePath(key1);
        assertNotNull(storedPath);
        assertTrue(Files.exists(storedPath));
        assertEquals("hello world", Files.readString(storedPath));
        assertEquals(1L, countFiles(tempDir.resolve("data").resolve("avatar")));
    }

    @Test
    void shouldReturnDifferentKeyForDifferentContent() {
        LocalFileStorage storage = newStorage(tempDir, List.of());

        String key1 = storage.save(textFile("a.txt", "hello"), "avatar");
        String key2 = storage.save(textFile("a.txt", "world"), "avatar");

        assertNotEquals(key1, key2);
    }

    @Test
    void shouldIgnoreUnsafeExtension() {
        LocalFileStorage storage = newStorage(tempDir, List.of());

        String key = storage.save(textFile("evil.tar.gz;", "hello"), "avatar");

        assertFalse(key.endsWith(".gz;"));
        assertFalse(key.endsWith(".tar"));
    }

    @Test
    void shouldRejectInvalidBizTypeAndTraversalKey() {
        LocalFileStorage storage = newStorage(tempDir, List.of("avatar"));

        assertThrows(IllegalArgumentException.class, () -> storage.save(textFile("a.txt", "hello"), "../avatar"));
        assertThrows(IllegalArgumentException.class, () -> storage.resolvePath("local:v1:avatar:../secret.txt"));
    }

    @Test
    void shouldResolveUrlForManagedKeyAndKeepExternalUrlUntouched() {
        LocalFileStorage storage = newStorage(tempDir, List.of());
        String key = storage.save(textFile("a.txt", "hello"), "avatar");

        assertTrue(storage.resolveUrl(key).startsWith("https://cdn.example.com/files/avatar/"));
        assertEquals("https://other.example.com/a.png", storage.resolveUrl("https://other.example.com/a.png"));
        assertEquals("https://cdn.example.com/files/avatar/2025/01/01/a.png", storage.resolveUrl("#avatar/2025/01/01/a.png"));
    }

    @Test
    void shouldDeleteManagedFileAndIgnoreExternalPath() {
        LocalFileStorage storage = newStorage(tempDir, List.of());
        String key = storage.save(textFile("a.txt", "hello"), "avatar");
        Path path = storage.resolvePath(key);

        assertTrue(storage.deleteByKey(key));
        assertNotNull(path);
        assertFalse(Files.exists(path));
        assertFalse(storage.deleteByKey("https://other.example.com/a.png"));
    }

    @Test
    void shouldHandleConcurrentDeduplication() throws ExecutionException, InterruptedException, IOException {
        LocalFileStorage storage = newStorage(tempDir, List.of());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<String> task = () -> storage.save(textFile("same.txt", "parallel"), "avatar");
            Future<String> future1 = executor.submit(task);
            Future<String> future2 = executor.submit(task);

            Set<String> keys = new HashSet<>();
            keys.add(future1.get());
            keys.add(future2.get());

            assertEquals(1, keys.size());
            assertEquals(1L, countFiles(tempDir.resolve("data").resolve("avatar")));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldExposeStoredFileInfo() {
        LocalFileStorage storage = newStorage(tempDir, List.of());

        StoredFileInfo info = storage.saveAndGetInfo(textFile("head.png", "hello"), "avatar");

        assertEquals(info.storageKey(), storage.save(textFile("head.png", "hello"), "avatar"));
        assertEquals("text/plain", info.contentType());
        assertEquals("head.png", info.originalFilename());
        assertTrue(info.url().startsWith("https://cdn.example.com/files/avatar/"));
        assertTrue(info.size() > 0);
        assertFalse(info.hash().isBlank());
    }

    @Test
    void shouldResolveLegacyPathInsideBaseDirectory() {
        LocalFileStorage storage = newStorage(tempDir, List.of());

        Path resolved = storage.resolvePath("#avatar/2025/01/01/a.png");

        assertEquals(tempDir.resolve("data").resolve("avatar/2025/01/01/a.png").normalize(), resolved);
        assertNull(storage.resolvePath("https://other.example.com/a.png"));
    }

    private LocalFileStorage newStorage(Path root, List<String> allowedBizTypes) {
        LocalStorageProperties properties = new LocalStorageProperties();
        properties.setBasePath(root.resolve("data").toString());
        properties.setTempDir(".tmp");
        properties.setPublicBaseUrl("https://cdn.example.com/files");
        properties.setAllowedBizTypes(allowedBizTypes);

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

    private long countFiles(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return 0L;
        }
        try (Stream<Path> stream = Files.walk(directory)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> !path.toString().contains("/.tmp/"))
                    .count();
        }
    }
}
