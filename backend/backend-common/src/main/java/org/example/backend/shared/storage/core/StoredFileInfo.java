/**
 * @file StoredFileInfo
 * @project pipker-do
 * @module 共享存储 / 文件保存结果
 * @description 表达上传文件保存后的系统 key、访问 URL、大小、内容类型、原始文件名和 hash。
 * @logic 1. 由存储驱动保存后创建；2. Controller 直接映射到上传响应 DTO。
 * @dependencies JDK record
 * @index_tags 上传结果, storageKey, 文件元数据, hash
 * @author holic512
 */
package org.example.backend.shared.storage.core;

public record StoredFileInfo(
        String storageKey,
        String url,
        long size,
        String contentType,
        String originalFilename,
        String hash
) {
}
