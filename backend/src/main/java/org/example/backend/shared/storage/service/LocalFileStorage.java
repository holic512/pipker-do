package org.example.backend.shared.storage.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class LocalFileStorage {

    private static final Pattern SAFE_EXTENSION_PATTERN = Pattern.compile("[A-Za-z0-9]{1,16}");

    private final LocalStorageProperties props;
    private final LegacyStorageKeyResolver legacyStorageKeyResolver;
    private final StorageUrlResolver storageUrlResolver;

    public LocalFileStorage(
            LocalStorageProperties props,
            LegacyStorageKeyResolver legacyStorageKeyResolver,
            StorageUrlResolver storageUrlResolver
    ) {
        this.props = props;
        this.legacyStorageKeyResolver = legacyStorageKeyResolver;
        this.storageUrlResolver = storageUrlResolver;
    }

    public String save(MultipartFile file, String bizType) {
        return saveAndGetInfo(file, bizType).storageKey();
    }

    public StoredFileInfo saveAndGetInfo(MultipartFile file, String bizType) {
        Objects.requireNonNull(file, "file 不能为空");

        Path basePath = resolveBasePath();
        String normalizedBizType = normalizeBizType(bizType);
        String extension = props.getKeepExtension() == null || props.getKeepExtension()
                ? extractSafeExtension(file.getOriginalFilename())
                : "";

        TempFileDigestResult tempFile = writeToTempFile(file, basePath);
        StorageKey storageKey = buildStorageKey(normalizedBizType, tempFile.hash(), extension);
        Path targetPath = resolveManagedPath(storageKey, basePath);

        try {
            storeIfAbsent(tempFile.path(), targetPath, tempFile.size());
            return new StoredFileInfo(
                    storageKey.asString(),
                    storageUrlResolver.resolveUrl(storageKey.asString()),
                    tempFile.size(),
                    file.getContentType(),
                    file.getOriginalFilename(),
                    tempFile.hash()
            );
        } finally {
            deleteQuietly(tempFile.path());
        }
    }

    public String resolveUrl(String stored) {
        return storageUrlResolver.resolveUrl(stored);
    }

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

    private TempFileDigestResult writeToTempFile(MultipartFile file, Path basePath) {
        Path tempDir = resolveTempDir(basePath);
        try {
            Files.createDirectories(tempDir);
            Path tempFile = Files.createTempFile(tempDir, "upload-", ".tmp");
            MessageDigest digest = createDigest();
            long size = 0L;

            try (InputStream inputStream = file.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                    size += read;
                    outputStream.write(buffer, 0, read);
                }
            } catch (IOException ex) {
                deleteQuietly(tempFile);
                throw ex;
            }

            return new TempFileDigestResult(tempFile, HexFormat.of().formatHex(digest.digest()), size);
        } catch (IOException e) {
            throw new RuntimeException("写入临时文件失败：" + e.getMessage(), e);
        }
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

    private Path resolveTempDir(Path basePath) {
        String configured = props.getTempDir();
        if (configured == null || configured.isBlank()) {
            return basePath.resolve(".tmp").normalize();
        }
        Path configuredPath = Paths.get(configured);
        Path resolved = configuredPath.isAbsolute() ? configuredPath.normalize() : basePath.resolve(configuredPath).normalize();
        if (!resolved.startsWith(basePath) && !configuredPath.isAbsolute()) {
            throw new IllegalArgumentException("tempDir 非法，超出存储根目录");
        }
        return resolved;
    }

    private MessageDigest createDigest() {
        String algorithm = props.getHashAlgorithm();
        if (algorithm == null || algorithm.isBlank()) {
            algorithm = "SHA-256";
        }
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("不支持的哈希算法：" + algorithm, e);
        }
    }

    private String normalizeBizType(String bizType) {
        StorageKey.validateBizType(bizType);
        List<String> allowedBizTypes = props.getAllowedBizTypes();
        if (allowedBizTypes != null && !allowedBizTypes.isEmpty() && !allowedBizTypes.contains(bizType)) {
            throw new IllegalArgumentException("bizType 不在允许列表内：" + bizType);
        }
        return bizType;
    }

    private String extractSafeExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        String ext = originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return SAFE_EXTENSION_PATTERN.matcher(ext).matches() ? ext : "";
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

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private record TempFileDigestResult(Path path, String hash, long size) {
    }
}
