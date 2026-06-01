/**
 * @file FileStorage
 * @project pipker-do
 * @module 共享存储 / 文件存储抽象
 * @description 定义业务侧统一文件存储接口，屏蔽本地存储与腾讯云 COS 存储差异。
 * @logic 1. 保存上传文件并返回受管存储键；2. 将存储键解析为可访问 URL；3. 删除受管文件并识别受管键。
 * @dependencies Spring MultipartFile, StoredFileInfo
 * @index_tags 文件存储, 存储抽象, 本地存储, 腾讯云COS
 * @author holic512
 */
package org.example.backend.shared.storage.core;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorage {

    String save(MultipartFile file, String bizType);

    StoredFileInfo saveAndGetInfo(MultipartFile file, String bizType);

    String resolveUrl(String stored);

    Path resolvePath(String stored);

    boolean deleteByKey(String stored);

    boolean isManagedKey(String value);
}
