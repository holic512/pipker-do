/**
 * @file FileUploadProcessor
 * @project pipker-do
 * @module 共享存储 / 上传文件预处理
 * @description 统一处理上传临时文件写入、图片压缩、SHA-256 计算和扩展名提取。
 * @logic 1. 写入临时文件；2. 对图片执行保守压缩；3. 基于最终文件计算 hash 和 size。
 * @dependencies LocalStorageProperties, ImageCompressionService, MultipartFile
 * @index_tags 文件上传, 临时文件, SHA-256, 图片压缩, 公共工具
 * @author holic512
 */
package org.example.backend.shared.storage.core;

import org.example.backend.shared.storage.config.LocalStorageProperties;
import org.example.backend.shared.storage.image.ImageCompressionService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class FileUploadProcessor {

    private static final Pattern SAFE_EXTENSION_PATTERN = Pattern.compile("[A-Za-z0-9]{1,16}");

    private final LocalStorageProperties localProperties;
    private final ImageCompressionService imageCompressionService;

    public FileUploadProcessor(LocalStorageProperties localProperties, ImageCompressionService imageCompressionService) {
        this.localProperties = localProperties;
        this.imageCompressionService = imageCompressionService;
    }

    public ProcessedUpload process(MultipartFile file, Path preferredBasePath) {
        Path tempFile = writeToTempFile(file, resolveTempDir(preferredBasePath));
        try {
            imageCompressionService.compressIfBeneficial(tempFile, file.getContentType(), file.getOriginalFilename());
            return digestProcessedFile(tempFile, file);
        } catch (RuntimeException e) {
            deleteQuietly(tempFile);
            throw e;
        }
    }

    public void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private Path writeToTempFile(MultipartFile file, Path tempDir) {
        try {
            Files.createDirectories(tempDir);
            Path tempFile = Files.createTempFile(tempDir, "upload-", ".tmp");
            try (InputStream inputStream = file.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            } catch (IOException ex) {
                deleteQuietly(tempFile);
                throw ex;
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("写入临时文件失败：" + e.getMessage(), e);
        }
    }

    private ProcessedUpload digestProcessedFile(Path tempFile, MultipartFile file) {
        MessageDigest digest = createDigest();
        long size = 0L;
        try (InputStream inputStream = Files.newInputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
                size += read;
            }
        } catch (IOException e) {
            throw new RuntimeException("读取临时文件失败：" + e.getMessage(), e);
        }
        String extension = Boolean.FALSE.equals(localProperties.getKeepExtension())
                ? ""
                : extractSafeExtension(file.getOriginalFilename());
        return new ProcessedUpload(
                tempFile,
                HexFormat.of().formatHex(digest.digest()),
                size,
                extension,
                file.getContentType(),
                file.getOriginalFilename()
        );
    }

    private Path resolveTempDir(Path preferredBasePath) {
        String configured = localProperties.getTempDir();
        if (configured == null || configured.isBlank()) {
            configured = ".tmp";
        }
        Path configuredPath = Paths.get(configured);
        if (configuredPath.isAbsolute()) {
            return configuredPath.normalize();
        }
        if (preferredBasePath != null) {
            Path resolved = preferredBasePath.resolve(configuredPath).normalize();
            if (!resolved.startsWith(preferredBasePath)) {
                throw new IllegalArgumentException("tempDir 非法，超出存储根目录");
            }
            return resolved;
        }
        String basePath = localProperties.getBasePath();
        if (basePath != null && !basePath.isBlank()) {
            Path normalizedBasePath = Paths.get(basePath).toAbsolutePath().normalize();
            Path resolved = normalizedBasePath.resolve(configuredPath).normalize();
            if (!resolved.startsWith(normalizedBasePath)) {
                throw new IllegalArgumentException("tempDir 非法，超出存储根目录");
            }
            return resolved;
        }
        try {
            return Paths.get(System.getProperty("java.io.tmpdir"), "pipkerdo-storage", configured).normalize();
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("storage.local.temp-dir 非法: " + configured, e);
        }
    }

    private MessageDigest createDigest() {
        String algorithm = localProperties.getHashAlgorithm();
        if (algorithm == null || algorithm.isBlank()) {
            algorithm = "SHA-256";
        }
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("不支持的哈希算法：" + algorithm, e);
        }
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
}
