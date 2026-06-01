/**
 * @file CosFileStorage
 * @project pipker-do
 * @module 共享存储 / 腾讯云 COS 文件存储
 * @description 基于腾讯云 COS 私有桶保存上传文件，并为受管存储键生成临时签名访问 URL。
 * @logic 1. 通过 FileUploadProcessor 完成压缩与 hash；2. 生成 pipker# 系统 key 和 COS 对象 key；3. 为系统/历史 key 生成签名 URL。
 * @dependencies Tencent COS SDK: COSClient, CosStorageProperties, LocalStorageProperties, FileUploadProcessor
 * @index_tags 腾讯云COS, pipker#, 私有桶, 签名URL, 图片压缩
 * @author holic512
 */
package org.example.backend.shared.storage.driver.cos;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import org.example.backend.shared.storage.config.CosStorageProperties;
import org.example.backend.shared.storage.config.LocalStorageProperties;
import org.example.backend.shared.storage.core.FileStorage;
import org.example.backend.shared.storage.core.FileUploadProcessor;
import org.example.backend.shared.storage.core.LegacyStorageKeyResolver;
import org.example.backend.shared.storage.core.ProcessedUpload;
import org.example.backend.shared.storage.core.StorageKey;
import org.example.backend.shared.storage.core.StoredFileInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "cos")
public class CosFileStorage implements FileStorage {

    private final COSClient cosClient;
    private final CosStorageProperties cosProperties;
    private final LocalStorageProperties localProperties;
    private final LegacyStorageKeyResolver legacyStorageKeyResolver;
    private final FileUploadProcessor fileUploadProcessor;

    public CosFileStorage(COSClient cosClient,
                          CosStorageProperties cosProperties,
                          LocalStorageProperties localProperties,
                          LegacyStorageKeyResolver legacyStorageKeyResolver,
                          FileUploadProcessor fileUploadProcessor) {
        this.cosClient = cosClient;
        this.cosProperties = cosProperties;
        this.localProperties = localProperties;
        this.legacyStorageKeyResolver = legacyStorageKeyResolver;
        this.fileUploadProcessor = fileUploadProcessor;
    }

    @Override
    public String save(MultipartFile file, String bizType) {
        return saveAndGetInfo(file, bizType).storageKey();
    }

    @Override
    public StoredFileInfo saveAndGetInfo(MultipartFile file, String bizType) {
        Objects.requireNonNull(file, "file 不能为空");

        String normalizedBizType = normalizeBizType(bizType);
        ProcessedUpload processedUpload = fileUploadProcessor.process(file, null);
        StorageKey storageKey = buildStorageKey(normalizedBizType, processedUpload.hash(), processedUpload.extension());
        String objectKey = storageKey.toRelativeStoragePath();

        try {
            storeIfAbsent(processedUpload.path(), objectKey, processedUpload.size());
            return new StoredFileInfo(
                    storageKey.asString(),
                    resolveUrl(storageKey.asString()),
                    processedUpload.size(),
                    processedUpload.contentType(),
                    processedUpload.originalFilename(),
                    processedUpload.hash()
            );
        } finally {
            fileUploadProcessor.deleteQuietly(processedUpload.path());
        }
    }

    @Override
    public String resolveUrl(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        String objectKey = resolveObjectKey(stored);
        if (objectKey == null) {
            return stored;
        }
        Date expiration = new Date(System.currentTimeMillis() + signedUrlExpireMillis());
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                requireBucket(),
                objectKey,
                HttpMethodName.GET
        );
        request.setExpiration(expiration);
        URL url = cosClient.generatePresignedUrl(request);
        return url.toString();
    }

    @Override
    public Path resolvePath(String stored) {
        return null;
    }

    @Override
    public boolean deleteByKey(String stored) {
        String objectKey = resolveObjectKey(stored);
        if (objectKey == null) {
            return false;
        }
        cosClient.deleteObject(requireBucket(), objectKey);
        return true;
    }

    @Override
    public boolean isManagedKey(String value) {
        return StorageKey.isManaged(value) || legacyStorageKeyResolver.isLegacyKey(value);
    }

    private void storeIfAbsent(Path tempPath, String objectKey, long expectedSize) {
        String bucket = requireBucket();
        if (cosClient.doesObjectExist(bucket, objectKey)) {
            ObjectMetadata metadata = cosClient.getObjectMetadata(bucket, objectKey);
            if (metadata.getContentLength() == expectedSize) {
                return;
            }
            throw new IllegalStateException("检测到同 key 异常 COS 对象：" + objectKey);
        }
        cosClient.putObject(new PutObjectRequest(bucket, objectKey, tempPath.toFile()));
    }

    private StorageKey buildStorageKey(String bizType, String hash, String extension) {
        StringBuilder relativePath = new StringBuilder();
        int shardDepth = defaultIfInvalid(localProperties.getShardDepth(), 2);
        int shardWidth = defaultIfInvalid(localProperties.getShardWidth(), 2);
        int maxShardChars = shardDepth * shardWidth;
        if (hash.length() < maxShardChars) {
            throw new IllegalStateException("hash 长度不足以生成分片目录");
        }
        for (int i = 0; i < shardDepth; i++) {
            int start = i * shardWidth;
            int end = start + shardWidth;
            relativePath.append(hash, start, end).append("/");
        }
        relativePath.append(hash);
        if (!extension.isBlank()) {
            relativePath.append(".").append(extension);
        }
        return StorageKey.of(bizType, relativePath.toString());
    }

    private String resolveObjectKey(String stored) {
        if (StorageKey.isManaged(stored)) {
            return StorageKey.parse(stored).toRelativeStoragePath();
        }
        if (legacyStorageKeyResolver.isLegacyKey(stored)) {
            return legacyStorageKeyResolver.resolveRelativePath(stored);
        }
        return null;
    }

    private String normalizeBizType(String bizType) {
        StorageKey.validateBizType(bizType);
        List<String> allowedBizTypes = localProperties.getAllowedBizTypes();
        if (allowedBizTypes != null && !allowedBizTypes.isEmpty() && !allowedBizTypes.contains(bizType)) {
            throw new IllegalArgumentException("bizType 不在允许列表内：" + bizType);
        }
        return bizType;
    }

    private long signedUrlExpireMillis() {
        long seconds = cosProperties.getSignedUrlExpireSeconds() == null || cosProperties.getSignedUrlExpireSeconds() <= 0
                ? 1800L
                : cosProperties.getSignedUrlExpireSeconds();
        return Duration.ofSeconds(seconds).toMillis();
    }

    private String requireBucket() {
        String bucket = cosProperties.getBucket();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("请在配置中设置 storage.cos.bucket");
        }
        return bucket;
    }

    private int defaultIfInvalid(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

}
