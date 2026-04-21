package org.example.backend.util.file;

import org.springframework.stereotype.Component;

@Component
public class StorageUrlResolver {

    private final LocalStorageProperties properties;
    private final LegacyStorageKeyResolver legacyStorageKeyResolver;

    public StorageUrlResolver(LocalStorageProperties properties, LegacyStorageKeyResolver legacyStorageKeyResolver) {
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
