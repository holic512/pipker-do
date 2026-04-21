package org.example.backend.util.file;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 新版结构化存储键：local:v1:{bizType}:{relativePath}
 */
public final class StorageKey {

    public static final String DRIVER = "local";
    public static final String VERSION = "v1";

    private static final Pattern BIZ_TYPE_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");
    private static final Pattern RELATIVE_PATH_PATTERN = Pattern.compile("[A-Za-z0-9._/-]+");

    private final String bizType;
    private final String relativePath;

    private StorageKey(String bizType, String relativePath) {
        this.bizType = bizType;
        this.relativePath = relativePath;
    }

    public static StorageKey of(String bizType, String relativePath) {
        validateBizType(bizType);
        validateRelativePath(relativePath);
        return new StorageKey(bizType, relativePath);
    }

    public static StorageKey parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("storage key 不能为空");
        }
        String[] parts = value.split(":", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("非法 storage key: " + value);
        }
        if (!DRIVER.equals(parts[0])) {
            throw new IllegalArgumentException("不支持的 storage driver: " + parts[0]);
        }
        if (!VERSION.equals(parts[1])) {
            throw new IllegalArgumentException("不支持的 storage key 版本: " + parts[1]);
        }
        return of(parts[2], parts[3]);
    }

    public static boolean isManaged(String value) {
        return value != null && value.startsWith(DRIVER + ":" + VERSION + ":");
    }

    public String asString() {
        return DRIVER + ":" + VERSION + ":" + bizType + ":" + relativePath;
    }

    public String getBizType() {
        return bizType;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String toRelativeStoragePath() {
        return bizType + "/" + relativePath;
    }

    public static void validateBizType(String bizType) {
        if (bizType == null || bizType.isBlank()) {
            throw new IllegalArgumentException("bizType 不能为空");
        }
        if (!BIZ_TYPE_PATTERN.matcher(bizType).matches()) {
            throw new IllegalArgumentException("bizType 仅允许字母、数字、点、下划线、中划线");
        }
    }

    private static void validateRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("relativePath 不能为空");
        }
        if (relativePath.startsWith("/") || relativePath.endsWith("/")) {
            throw new IllegalArgumentException("relativePath 不能以 / 开头或结尾");
        }
        if (relativePath.contains("..") || relativePath.contains("\\")) {
            throw new IllegalArgumentException("relativePath 非法");
        }
        if (!RELATIVE_PATH_PATTERN.matcher(relativePath).matches()) {
            throw new IllegalArgumentException("relativePath 包含非法字符");
        }
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StorageKey that)) {
            return false;
        }
        return Objects.equals(bizType, that.bizType) && Objects.equals(relativePath, that.relativePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bizType, relativePath);
    }
}
