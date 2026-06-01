/**
 * @file ImageCompressionService
 * @project pipker-do
 * @module 共享存储 / 图片压缩服务
 * @description 对上传图片执行保守压缩，减少本地和 COS 文件占用。
 * @logic 1. 仅处理 JPEG/PNG；2. 按配置限制最大宽高并调整 JPEG 质量；3. 压缩结果更小时替换原临时文件。
 * @dependencies Thumbnailator, ImageCompressionProperties, ImageIO
 * @index_tags 图片压缩, JPEG压缩, PNG缩放, 上传预处理
 * @author holic512
 */
package org.example.backend.shared.storage.image;

import net.coobird.thumbnailator.Thumbnails;
import org.example.backend.shared.storage.config.ImageCompressionProperties;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

@Component
public class ImageCompressionService {

    private final ImageCompressionProperties properties;

    public ImageCompressionService(ImageCompressionProperties properties) {
        this.properties = properties;
    }

    public void compressIfBeneficial(Path source, String contentType, String originalFilename) {
        if (source == null || !Boolean.TRUE.equals(properties.getEnabled())) {
            return;
        }
        String format = resolveCompressibleFormat(contentType, originalFilename);
        if (format == null) {
            return;
        }

        Path candidate = null;
        try {
            long originalSize = Files.size(source);
            BufferedImage image = ImageIO.read(source.toFile());
            if (image == null) {
                return;
            }

            candidate = Files.createTempFile(source.getParent(), "upload-compress-", "." + format);
            writeCompressedImage(source, candidate, image, format);

            long compressedSize = Files.size(candidate);
            if (compressedSize > 0 && isWorthReplacing(originalSize, compressedSize)) {
                Files.move(candidate, source, StandardCopyOption.REPLACE_EXISTING);
                candidate = null;
            }
        } catch (IOException e) {
            throw new RuntimeException("压缩上传图片失败：" + e.getMessage(), e);
        } finally {
            deleteQuietly(candidate);
        }
    }

    private void writeCompressedImage(Path source, Path candidate, BufferedImage image, String format) throws IOException {
        int maxWidth = defaultIfInvalid(properties.getMaxWidth(), 1600);
        int maxHeight = defaultIfInvalid(properties.getMaxHeight(), 1600);

        var builder = Thumbnails.of(source.toFile())
                .outputFormat(format)
                .allowOverwrite(true);
        if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
            builder.size(maxWidth, maxHeight);
        } else {
            builder.scale(1.0D);
        }
        if ("jpg".equals(format)) {
            builder.outputQuality(clampQuality(properties.getJpegQuality()));
        }
        builder.toFile(candidate.toFile());
    }

    private boolean isWorthReplacing(long originalSize, long compressedSize) {
        if (compressedSize >= originalSize) {
            return false;
        }
        double minSavingRatio = properties.getMinSavingRatio() == null ? 0.05D : properties.getMinSavingRatio();
        if (minSavingRatio <= 0) {
            return true;
        }
        double savedRatio = (originalSize - compressedSize) / (double) originalSize;
        return savedRatio >= minSavingRatio;
    }

    private String resolveCompressibleFormat(String contentType, String originalFilename) {
        String normalizedContentType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if ("image/jpeg".equals(normalizedContentType) || "image/jpg".equals(normalizedContentType)) {
            return "jpg";
        }
        if ("image/png".equals(normalizedContentType)) {
            return "png";
        }
        String extension = extractExtension(originalFilename);
        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            return "jpg";
        }
        if ("png".equals(extension)) {
            return "png";
        }
        return null;
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private double clampQuality(Double configuredQuality) {
        if (configuredQuality == null) {
            return 0.82D;
        }
        return Math.max(0.01D, Math.min(1.0D, configuredQuality));
    }

    private int defaultIfInvalid(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
