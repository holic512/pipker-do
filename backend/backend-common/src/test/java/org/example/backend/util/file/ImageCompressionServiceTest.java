/**
 * @file ImageCompressionServiceTest
 * @project pipker-do
 * @module 共享存储 / 图片压缩测试
 * @description 验证上传图片压缩服务对 JPEG、PNG 和非图片文件的处理边界。
 * @logic 1. 大 JPEG 按最大尺寸缩放并降低体积；2. PNG 保留透明通道；3. 非图片文件跳过压缩。
 * @dependencies JUnit 5, ImageIO, ImageCompressionService
 * @index_tags 图片压缩测试, JPEG, PNG透明, 非图片跳过
 * @author holic512
 */
package org.example.backend.util.file;

import org.example.backend.shared.storage.config.ImageCompressionProperties;
import org.example.backend.shared.storage.image.ImageCompressionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageCompressionServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldResizeLargeJpegAndReduceSize() throws IOException {
        Path imagePath = tempDir.resolve("large.jpg");
        writeJpeg(noisyImage(1200, 900), imagePath, 1.0F);
        long originalSize = Files.size(imagePath);

        ImageCompressionProperties properties = defaultProperties();
        properties.setMaxWidth(320);
        properties.setMaxHeight(320);
        new ImageCompressionService(properties).compressIfBeneficial(imagePath, "image/jpeg", "large.jpg");

        BufferedImage compressed = ImageIO.read(imagePath.toFile());
        assertTrue(Files.size(imagePath) < originalSize);
        assertTrue(compressed.getWidth() <= 320);
        assertTrue(compressed.getHeight() <= 320);
    }

    @Test
    void shouldKeepPngTransparency() throws IOException {
        Path imagePath = tempDir.resolve("transparent.png");
        BufferedImage image = new BufferedImage(480, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(40, 120, 200, 0));
        graphics.fillRect(0, 0, 480, 320);
        graphics.setColor(new Color(200, 40, 120, 180));
        graphics.fillOval(40, 40, 240, 180);
        graphics.dispose();
        ImageIO.write(image, "png", imagePath.toFile());

        ImageCompressionProperties properties = defaultProperties();
        properties.setMaxWidth(160);
        properties.setMaxHeight(160);
        properties.setMinSavingRatio(0D);
        new ImageCompressionService(properties).compressIfBeneficial(imagePath, "image/png", "transparent.png");

        BufferedImage compressed = ImageIO.read(imagePath.toFile());
        assertTrue(compressed.getWidth() <= 160);
        assertTrue(compressed.getColorModel().hasAlpha());
    }

    @Test
    void shouldSkipNonImageFile() throws IOException {
        Path textPath = tempDir.resolve("a.txt");
        byte[] original = "hello".getBytes(StandardCharsets.UTF_8);
        Files.write(textPath, original);

        new ImageCompressionService(defaultProperties()).compressIfBeneficial(textPath, "text/plain", "a.txt");

        assertArrayEquals(original, Files.readAllBytes(textPath));
    }

    @Test
    void shouldKeepOriginalWhenSavingThresholdNotReached() throws IOException {
        Path imagePath = tempDir.resolve("small.jpg");
        writeJpeg(noisyImage(80, 80), imagePath, 0.85F);
        long originalSize = Files.size(imagePath);

        ImageCompressionProperties properties = defaultProperties();
        properties.setMinSavingRatio(1.0D);
        new ImageCompressionService(properties).compressIfBeneficial(imagePath, "image/jpeg", "small.jpg");

        assertEquals(originalSize, Files.size(imagePath));
    }

    private ImageCompressionProperties defaultProperties() {
        ImageCompressionProperties properties = new ImageCompressionProperties();
        properties.setEnabled(true);
        properties.setJpegQuality(0.72D);
        properties.setMinSavingRatio(0.01D);
        return properties;
    }

    private BufferedImage noisyImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(20260602L);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, random.nextInt(0xFFFFFF));
            }
        }
        return image;
    }

    private void writeJpeg(BufferedImage image, Path path, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        try (ImageOutputStream output = ImageIO.createImageOutputStream(path.toFile())) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }
}
