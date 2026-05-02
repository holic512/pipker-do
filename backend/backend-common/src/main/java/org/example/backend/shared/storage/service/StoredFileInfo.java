package org.example.backend.shared.storage.service;

public record StoredFileInfo(
        String storageKey,
        String url,
        long size,
        String contentType,
        String originalFilename,
        String hash
) {
}
