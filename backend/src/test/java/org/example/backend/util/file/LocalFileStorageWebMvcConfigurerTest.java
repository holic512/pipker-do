package org.example.backend.util.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageWebMvcConfigurerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldBuildResourcePatternFromPublicBaseUrl() {
        assertEquals("/files/**",
                LocalFileStorageWebMvcConfigurer.toResourceHandlerPattern("http://localhost:8080/files"));
        assertEquals("/static/files/**",
                LocalFileStorageWebMvcConfigurer.toResourceHandlerPattern("https://cdn.example.com/static/files/"));
    }

    @Test
    void shouldFallbackToDefaultFilesPatternWhenUrlHasNoPath() {
        assertEquals("/files/**",
                LocalFileStorageWebMvcConfigurer.toResourceHandlerPattern("https://cdn.example.com"));
    }

    @Test
    void shouldBuildFileResourceLocation() {
        String location = LocalFileStorageWebMvcConfigurer.toFileResourceLocation(tempDir.toString());

        assertTrue(location.startsWith("file:"));
        assertTrue(location.endsWith("/"));
    }

    @Test
    void shouldRejectInvalidConfig() {
        assertThrows(IllegalArgumentException.class,
                () -> LocalFileStorageWebMvcConfigurer.toResourceHandlerPattern("://bad-url"));
        assertThrows(IllegalStateException.class,
                () -> LocalFileStorageWebMvcConfigurer.toFileResourceLocation(" "));
    }
}
