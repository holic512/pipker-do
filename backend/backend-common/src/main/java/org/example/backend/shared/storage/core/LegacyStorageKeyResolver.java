/**
 * @file LegacyStorageKeyResolver
 * @project pipker-do
 * @module 共享存储 / 历史 key 兼容
 * @description 解析旧版 #relativePath 文件 key，兼容历史数据库值。
 * @logic 1. 识别 # 前缀；2. 去除多余斜杠并规范化路径；3. 拒绝绝对路径和目录穿越。
 * @dependencies JDK Path
 * @index_tags legacy key, #文件路径, 文件兼容, 路径校验
 * @author holic512
 */
package org.example.backend.shared.storage.core;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class LegacyStorageKeyResolver {

    public static final String TAG_PREFIX = "#";

    public boolean isLegacyKey(String value) {
        return value != null && value.startsWith(TAG_PREFIX);
    }

    public String resolveRelativePath(String value) {
        if (!isLegacyKey(value)) {
            return null;
        }
        String relativePath = value.substring(TAG_PREFIX.length());
        while (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        if (relativePath.isBlank()) {
            throw new IllegalArgumentException("非法 legacy storage key: " + value);
        }
        Path normalized = Paths.get(relativePath).normalize();
        String normalizedString = normalized.toString().replace("\\", "/");
        if (normalizedString.isBlank() || normalized.isAbsolute() || normalizedString.startsWith("../") || normalizedString.contains("/../")) {
            throw new IllegalArgumentException("非法 legacy storage key: " + value);
        }
        return normalizedString;
    }
}
