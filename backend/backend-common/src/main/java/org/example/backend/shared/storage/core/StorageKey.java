/**
 * @file StorageKey
 * @project pipker-do
 * @module 共享存储 / 结构化存储键
 * @description 解析和生成驱动无关的系统文件 key，并兼容历史 local/cos 存储键。
 * @logic 1. 新 key 使用 pipker#{bizType}/{relativePath}；2. 兼容 {driver}:v1:{bizType}:{relativePath}；3. 校验业务目录和相对路径。
 * @dependencies JDK: Pattern, Set
 * @index_tags storageKey, pipker#, local:v1, cos:v1, 文件路径校验
 * @author holic512
 */
package org.example.backend.shared.storage.core;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class StorageKey {

    public static final String LOCAL_DRIVER = "local";
    public static final String COS_DRIVER = "cos";
    public static final String SYSTEM_DRIVER = "pipker";
    public static final String SYSTEM_PREFIX = "pipker#";
    public static final String VERSION = "v1";

    private static final Set<String> SUPPORTED_DRIVERS = Set.of(LOCAL_DRIVER, COS_DRIVER, SYSTEM_DRIVER);
    private static final Pattern BIZ_TYPE_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");
    private static final Pattern RELATIVE_PATH_PATTERN = Pattern.compile("[A-Za-z0-9._/-]+");

    private final String driver;
    private final String bizType;
    private final String relativePath;

    private StorageKey(String driver, String bizType, String relativePath) {
        this.driver = driver;
        this.bizType = bizType;
        this.relativePath = relativePath;
    }

    public static StorageKey of(String bizType, String relativePath) {
        return ofSystem(bizType, relativePath);
    }

    public static StorageKey ofSystem(String bizType, String relativePath) {
        return of(SYSTEM_DRIVER, bizType, relativePath);
    }

    public static StorageKey ofLocal(String bizType, String relativePath) {
        return of(LOCAL_DRIVER, bizType, relativePath);
    }

    public static StorageKey ofCos(String bizType, String relativePath) {
        return of(COS_DRIVER, bizType, relativePath);
    }

    public static StorageKey of(String driver, String bizType, String relativePath) {
        validateDriver(driver);
        validateBizType(bizType);
        validateRelativePath(relativePath);
        return new StorageKey(driver, bizType, relativePath);
    }

    public static StorageKey parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("storage key 不能为空");
        }
        int systemPrefixIndex = value.indexOf(SYSTEM_PREFIX);
        if (systemPrefixIndex >= 0) {
            return parseRelativeStoragePath(value.substring(systemPrefixIndex + SYSTEM_PREFIX.length()), SYSTEM_DRIVER);
        }
        String[] parts = value.split(":", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("非法 storage key: " + value);
        }
        if (!SUPPORTED_DRIVERS.contains(parts[0])) {
            throw new IllegalArgumentException("不支持的 storage driver: " + parts[0]);
        }
        if (!VERSION.equals(parts[1])) {
            throw new IllegalArgumentException("不支持的 storage key 版本: " + parts[1]);
        }
        return of(parts[0], parts[2], parts[3]);
    }

    public static StorageKey parseRelativeStoragePath(String value) {
        return parseRelativeStoragePath(value, SYSTEM_DRIVER);
    }

    private static StorageKey parseRelativeStoragePath(String value, String driver) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("relative storage path 不能为空");
        }
        String normalized = value;
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        int separatorIndex = normalized.indexOf('/');
        if (separatorIndex <= 0 || separatorIndex == normalized.length() - 1) {
            throw new IllegalArgumentException("非法 relative storage path: " + value);
        }
        return of(driver, normalized.substring(0, separatorIndex), normalized.substring(separatorIndex + 1));
    }

    public static boolean isManaged(String value) {
        return value != null && (isSystemManaged(value) || isLocalManaged(value) || isCosManaged(value));
    }

    public static boolean isSystemManaged(String value) {
        return value != null && value.contains(SYSTEM_PREFIX);
    }

    public static boolean isLocalManaged(String value) {
        return value != null && value.startsWith(LOCAL_DRIVER + ":" + VERSION + ":");
    }

    public static boolean isCosManaged(String value) {
        return value != null && value.startsWith(COS_DRIVER + ":" + VERSION + ":");
    }

    public String asString() {
        if (SYSTEM_DRIVER.equals(driver)) {
            return SYSTEM_PREFIX + toRelativeStoragePath();
        }
        return driver + ":" + VERSION + ":" + bizType + ":" + relativePath;
    }

    public String getDriver() {
        return driver;
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

    private static void validateDriver(String driver) {
        if (driver == null || driver.isBlank()) {
            throw new IllegalArgumentException("storage driver 不能为空");
        }
        if (!SUPPORTED_DRIVERS.contains(driver)) {
            throw new IllegalArgumentException("不支持的 storage driver: " + driver);
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
        return Objects.equals(driver, that.driver)
                && Objects.equals(bizType, that.bizType)
                && Objects.equals(relativePath, that.relativePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driver, bizType, relativePath);
    }
}
