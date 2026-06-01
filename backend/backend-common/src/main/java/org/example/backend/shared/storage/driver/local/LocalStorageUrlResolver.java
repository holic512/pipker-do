/**
 * @file LocalStorageUrlResolver
 * @project pipker-do
 * @module 共享存储 / 本地存储 URL 解析
 * @description 将系统文件 key 和历史 key 解析为本地代理访问 URL。
 * @logic 1. 解析 pipker#/local:v1/cos:v1/#；2. 拼接 storage.local.public-base-url；3. 非受管值原样返回。
 * @dependencies LocalStorageProperties, LegacyStorageKeyResolver, StorageKey
 * @index_tags 本地URL解析, pipker#, legacy key, public-base-url
 * @author holic512
 */
package org.example.backend.shared.storage.driver.local;

import org.example.backend.shared.storage.config.LocalStorageProperties;
import org.example.backend.shared.storage.core.LegacyStorageKeyResolver;
import org.example.backend.shared.storage.core.StorageKey;
import org.springframework.stereotype.Component;

@Component
public class LocalStorageUrlResolver {

    private final LocalStorageProperties properties;
    private final LegacyStorageKeyResolver legacyStorageKeyResolver;

    public LocalStorageUrlResolver(LocalStorageProperties properties, LegacyStorageKeyResolver legacyStorageKeyResolver) {
        this.properties = properties;
        this.legacyStorageKeyResolver = legacyStorageKeyResolver;
    }

    public String resolveUrl(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        String relativePath = resolveRelativePath(stored);
        if (relativePath == null) {
            return stored;
        }
        String base = requireNonEmpty(properties.getPublicBaseUrl(), "请在配置中设置 storage.local.public-base-url");
        if (!base.endsWith("/")) {
            base += "/";
        }
        return base + relativePath;
    }

    public String resolveRelativePath(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        if (StorageKey.isManaged(stored)) {
            return StorageKey.parse(stored).toRelativeStoragePath();
        }
        if (legacyStorageKeyResolver.isLegacyKey(stored)) {
            return legacyStorageKeyResolver.resolveRelativePath(stored);
        }
        return null;
    }

    private String requireNonEmpty(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
        return value;
    }
}
