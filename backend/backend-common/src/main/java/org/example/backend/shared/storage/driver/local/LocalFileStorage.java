/**
 * @file LocalFileStorage
 * @project pipker-do
 * @module 共享存储 / 本地文件存储
 * @description 基于服务器本地目录保存上传文件，并将系统文件 key 解析为本地代理 URL。
 * @logic 1. 通过 FileUploadProcessor 完成压缩与 hash；2. 生成 pipker# 系统 key；3. 复用同内容文件并支持旧 key 解析删除。
 * @dependencies LocalStorageProperties, LocalStorageUrlResolver, LegacyStorageKeyResolver, FileUploadProcessor
 * @index_tags 本地文件存储, pipker#, 文件上传, 哈希分片, 图片压缩
 * @author holic512
 */
package org.example.backend.shared.storage.driver.local;

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

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

@Component
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

    private final LocalStorageProperties props;
    private final LegacyStorageKeyResolver legacyStorageKeyResolver;
    private final LocalStorageUrlResolver storageUrlResolver;
    private final FileUploadProcessor fileUploadProcessor;

    public LocalFileStorage(
            LocalStorageProperties props,
            LegacyStorageKeyResolver legacyStorageKeyResolver,
            LocalStorageUrlResolver storageUrlResolver,
            FileUploadProcessor fileUploadProcessor
    ) {
        this.props = props;
        this.legacyStorageKeyResolver = legacyStorageKeyResolver;
        this.storageUrlResolver = storageUrlResolver;
        this.fileUploadProcessor = fileUploadProcessor;
    }

    @Override
    public String save(MultipartFile file, String bizType) {
        return saveAndGetInfo(file, bizType).storageKey();
    }

    @Override
    public StoredFileInfo saveAndGetInfo(MultipartFile file, String bizType) {
        Objects.requireNonNull(file, "file 不能为空");

        Path basePath = resolveBasePath();
        String normalizedBizType = normalizeBizType(bizType);

        ProcessedUpload processedUpload = fileUploadProcessor.process(file, basePath);
        StorageKey storageKey = buildStorageKey(normalizedBizType, processedUpload.hash(), processedUpload.extension());
        Path targetPath = resolveManagedPath(storageKey, basePath);

        try {
            storeIfAbsent(processedUpload.path(), targetPath, processedUpload.size());
            return new StoredFileInfo(
                    storageKey.asString(),
                    storageUrlResolver.resolveUrl(storageKey.asString()),
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
        return storageUrlResolver.resolveUrl(stored);
    }

    @Override
    public Path resolvePath(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        String relativePath = storageUrlResolver.resolveRelativePath(stored);
        if (relativePath == null) {
            return null;
        }
        return resolveRelativePath(relativePath, resolveBasePath());
    }

    @Override
    public boolean deleteByKey(String stored) {
        Path path = resolvePath(stored);
        if (path == null) {
            return false;
        }
        try {
            return !Files.exists(path) || Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("删除文件失败：" + path, e);
        }
    }

    /**
     * @deprecated 请使用 deleteByKey
     */
    @Deprecated
    public boolean deleteByTag(String stored) {
        return deleteByKey(stored);
    }

    @Override
    public boolean isManagedKey(String value) {
        return StorageKey.isManaged(value) || legacyStorageKeyResolver.isLegacyKey(value);
    }

    /**
     * @deprecated 请使用 isManagedKey
     */
    @Deprecated
    public boolean isManagedTag(String value) {
        return isManagedKey(value);
    }

    private void storeIfAbsent(Path tempPath, Path targetPath, long expectedSize) {
        try {
            if (Files.exists(targetPath)) {
                validateExistingFile(targetPath, expectedSize);
                return;
            }

            Files.createDirectories(targetPath.getParent());
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                try {
                    Files.move(tempPath, targetPath);
                } catch (FileAlreadyExistsException fileAlreadyExistsException) {
                    validateExistingFile(targetPath, expectedSize);
                }
            } catch (FileAlreadyExistsException e) {
                validateExistingFile(targetPath, expectedSize);
            }
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败：" + e.getMessage(), e);
        }
    }

    private void validateExistingFile(Path targetPath, long expectedSize) throws IOException {
        if (Files.isRegularFile(targetPath) && Files.size(targetPath) == expectedSize) {
            return;
        }
        throw new IllegalStateException("检测到同 key 异常文件：" + targetPath);
    }

    private StorageKey buildStorageKey(String bizType, String hash, String extension) {
        StringBuilder relativePath = new StringBuilder();
        int shardDepth = defaultIfInvalid(props.getShardDepth(), 2);
        int shardWidth = defaultIfInvalid(props.getShardWidth(), 2);
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

    private Path resolveManagedPath(StorageKey storageKey, Path basePath) {
        return resolveRelativePath(storageKey.toRelativeStoragePath(), basePath);
    }

    private Path resolveRelativePath(String relativePath, Path basePath) {
        try {
            Path resolved = basePath.resolve(relativePath).normalize();
            if (!resolved.startsWith(basePath)) {
                throw new IllegalArgumentException("非法路径，超出存储根目录");
            }
            return resolved;
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("非法路径：" + relativePath, e);
        }
    }

    private Path resolveBasePath() {
        String basePath = requireNonEmpty(props.getBasePath(), "请在配置中设置 storage.local.base-path 或 storage.local.root-dir");
        return Paths.get(basePath).toAbsolutePath().normalize();
    }

    private String normalizeBizType(String bizType) {
        StorageKey.validateBizType(bizType);
        List<String> allowedBizTypes = props.getAllowedBizTypes();
        if (allowedBizTypes != null && !allowedBizTypes.isEmpty() && !allowedBizTypes.contains(bizType)) {
            throw new IllegalArgumentException("bizType 不在允许列表内：" + bizType);
        }
        return bizType;
    }

    private int defaultIfInvalid(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private String requireNonEmpty(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
        return value;
    }

}
