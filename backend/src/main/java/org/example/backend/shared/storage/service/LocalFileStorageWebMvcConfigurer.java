package org.example.backend.shared.storage.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class LocalFileStorageWebMvcConfigurer implements WebMvcConfigurer {

    private final LocalStorageProperties properties;

    public LocalFileStorageWebMvcConfigurer(LocalStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourcePattern = toResourceHandlerPattern(properties.getPublicBaseUrl());
        String location = toFileResourceLocation(properties.getBasePath());
        registry.addResourceHandler(resourcePattern)
                .addResourceLocations(location);
    }

    static String toResourceHandlerPattern(String publicBaseUrl) {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            throw new IllegalStateException("请在配置中设置 storage.local.public-base-url");
        }
        try {
            URI uri = new URI(publicBaseUrl);
            String path = uri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                path = "/files";
            }
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            while (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }
            return path + "/**";
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("storage.local.public-base-url 非法: " + publicBaseUrl, e);
        }
    }

    static String toFileResourceLocation(String basePath) {
        if (basePath == null || basePath.isBlank()) {
            throw new IllegalStateException("请在配置中设置 storage.local.base-path 或 storage.local.root-dir");
        }
        Path normalized = Paths.get(basePath).toAbsolutePath().normalize();
        String uri = normalized.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
