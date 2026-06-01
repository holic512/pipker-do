/**
 * @file CosFileStorageTest
 * @project pipker-do
 * @module 共享存储 / 腾讯云 COS 文件存储测试
 * @description 使用 mock COSClient 验证 COS 文件存储实现的上传、复用、签名 URL 和删除行为。
 * @logic 1. 上传生成 pipker# 系统 key；2. 已存在同大小对象复用且异常大小报错；3. pipker#/local/# 历史 key 可解析为 COS 签名 URL。
 * @dependencies JUnit 5, Mockito, Tencent COS SDK
 * @index_tags COS测试, 私有桶, 签名URL, 文件上传, 对象删除
 * @author holic512
 */
package org.example.backend.util.file;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import org.example.backend.shared.storage.config.CosStorageProperties;
import org.example.backend.shared.storage.config.ImageCompressionProperties;
import org.example.backend.shared.storage.config.LocalStorageProperties;
import org.example.backend.shared.storage.core.FileUploadProcessor;
import org.example.backend.shared.storage.core.LegacyStorageKeyResolver;
import org.example.backend.shared.storage.core.StoredFileInfo;
import org.example.backend.shared.storage.driver.cos.CosFileStorage;
import org.example.backend.shared.storage.image.ImageCompressionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CosFileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldUploadFileAndReturnCosStorageKey() throws MalformedURLException {
        COSClient cosClient = mockCosClient();
        CosFileStorage storage = newStorage(cosClient);

        StoredFileInfo info = storage.saveAndGetInfo(textFile("head.png", "hello"), "avatar");

        assertTrue(info.storageKey().startsWith("pipker#avatar/"));
        assertTrue(info.storageKey().endsWith(".png"));
        assertEquals("https://signed.example.com/object?sign=1", info.url());
        assertEquals("head.png", info.originalFilename());
        verify(cosClient).putObject(any(PutObjectRequest.class));
    }

    @Test
    void shouldReuseExistingObjectWhenSizeMatches() throws MalformedURLException {
        COSClient cosClient = mockCosClient();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(5);
        when(cosClient.doesObjectExist(eq("bucket-1250000000"), any(String.class))).thenReturn(true);
        when(cosClient.getObjectMetadata(eq("bucket-1250000000"), any(String.class))).thenReturn(metadata);
        CosFileStorage storage = newStorage(cosClient);

        StoredFileInfo info = storage.saveAndGetInfo(textFile("head.png", "hello"), "avatar");

        assertTrue(info.storageKey().startsWith("pipker#avatar/"));
        verify(cosClient, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void shouldRejectExistingObjectWithUnexpectedSize() throws MalformedURLException {
        COSClient cosClient = mockCosClient();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(99);
        when(cosClient.doesObjectExist(eq("bucket-1250000000"), any(String.class))).thenReturn(true);
        when(cosClient.getObjectMetadata(eq("bucket-1250000000"), any(String.class))).thenReturn(metadata);
        CosFileStorage storage = newStorage(cosClient);

        assertThrows(IllegalStateException.class,
                () -> storage.saveAndGetInfo(textFile("head.png", "hello"), "avatar"));
    }

    @Test
    void shouldResolveManagedAndLegacyKeysToSignedUrl() throws MalformedURLException {
        COSClient cosClient = mockCosClient();
        CosFileStorage storage = newStorage(cosClient);

        assertEquals("https://signed.example.com/object?sign=1",
                storage.resolveUrl("pipker#avatar/ab/cd/hash.png"));
        assertEquals("https://signed.example.com/object?sign=1",
                storage.resolveUrl("cos:v1:avatar:ab/cd/hash.png"));
        assertEquals("https://signed.example.com/object?sign=1",
                storage.resolveUrl("local:v1:avatar:ab/cd/hash.png"));
        assertEquals("https://signed.example.com/object?sign=1",
                storage.resolveUrl("#avatar/2025/01/01/a.png"));
        assertEquals("https://other.example.com/a.png",
                storage.resolveUrl("https://other.example.com/a.png"));
        verify(cosClient, times(4)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    void shouldDeleteManagedObjectAndIgnoreExternalUrl() throws MalformedURLException {
        COSClient cosClient = mockCosClient();
        CosFileStorage storage = newStorage(cosClient);

        assertTrue(storage.deleteByKey("pipker#avatar/ab/cd/hash.png"));
        assertFalse(storage.deleteByKey("https://other.example.com/a.png"));
        verify(cosClient).deleteObject("bucket-1250000000", "avatar/ab/cd/hash.png");
    }

    private COSClient mockCosClient() throws MalformedURLException {
        COSClient cosClient = mock(COSClient.class);
        when(cosClient.doesObjectExist(eq("bucket-1250000000"), any(String.class))).thenReturn(false);
        when(cosClient.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL("https://signed.example.com/object?sign=1"));
        return cosClient;
    }

    private CosFileStorage newStorage(COSClient cosClient) {
        LocalStorageProperties localProperties = new LocalStorageProperties();
        localProperties.setBasePath(tempDir.resolve("data").toString());
        localProperties.setTempDir(".tmp");
        localProperties.setAllowedBizTypes(List.of("avatar", "kyzz-question-bank-cover"));
        CosStorageProperties cosProperties = new CosStorageProperties();
        cosProperties.setBucket("bucket-1250000000");
        cosProperties.setSignedUrlExpireSeconds(1800L);
        ImageCompressionProperties imageCompressionProperties = new ImageCompressionProperties();
        imageCompressionProperties.setEnabled(false);
        FileUploadProcessor fileUploadProcessor = new FileUploadProcessor(
                localProperties,
                new ImageCompressionService(imageCompressionProperties)
        );
        return new CosFileStorage(cosClient, cosProperties, localProperties, new LegacyStorageKeyResolver(), fileUploadProcessor);
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
