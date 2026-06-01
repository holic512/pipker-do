/**
 * @file ProcessedUpload
 * @project pipker-do
 * @module 共享存储 / 上传预处理结果
 * @description 承载上传临时文件经过图片压缩后的最终文件信息。
 * @logic 1. 保存最终临时文件路径；2. 记录最终文件大小、hash、扩展名和原始元数据。
 * @dependencies JDK Path
 * @index_tags 上传预处理, 文件hash, 图片压缩, 临时文件
 * @author holic512
 */
package org.example.backend.shared.storage.core;

import java.nio.file.Path;

public record ProcessedUpload(
        Path path,
        String hash,
        long size,
        String extension,
        String contentType,
        String originalFilename
) {
}
