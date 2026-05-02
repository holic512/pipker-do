package org.example.backend.shared.storage.service;

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
