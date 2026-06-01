/**
 * @file ImageCompressionProperties
 * @project pipker-do
 * @module 共享存储 / 图片压缩配置
 * @description 绑定上传图片自动压缩参数，控制启用状态、最大尺寸、JPEG 质量和最小收益阈值。
 * @logic 1. 默认启用保守压缩；2. 限制最大宽高；3. 仅当压缩收益达到阈值时替换原文件。
 * @dependencies Spring Boot ConfigurationProperties
 * @index_tags 图片压缩, 上传优化, storage.image-compression, Thumbnailator
 * @author holic512
 */
package org.example.backend.shared.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "storage.image-compression")
public class ImageCompressionProperties {

    private Boolean enabled = true;

    private Integer maxWidth = 1600;

    private Integer maxHeight = 1600;

    private Double jpegQuality = 0.82D;

    private Double minSavingRatio = 0.05D;
}
