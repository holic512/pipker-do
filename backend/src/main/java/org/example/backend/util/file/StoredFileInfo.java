package org.example.backend.util.file;

public record StoredFileInfo(
        String storageKey,
        String url,
        long size,
        String contentType,
        String originalFilename,
        String hash
) {
}
