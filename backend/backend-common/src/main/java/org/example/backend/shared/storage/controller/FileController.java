/**
 * @file FileController
 * @project pipker-do
 * @module 共享存储 / 用户文件上传
 * @description 提供小程序用户侧头像上传接口，返回受管存储键和可访问 URL。
 * @logic 1. 校验头像大小与图片类型；2. 调用 FileStorage 保存文件；3. 返回 storageKey、url、size。
 * @dependencies API: /api/files/avatar, Service: FileStorage
 * @index_tags 头像上传, 用户文件, FileStorage, avatar
 * @author holic512
 */
package org.example.backend.shared.storage.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.storage.core.FileStorage;
import org.example.backend.shared.storage.core.StoredFileInfo;
import org.example.backend.shared.storage.dto.UploadAvatarResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final long MAX_AVATAR_SIZE = 2L * 1024 * 1024;

    private final FileStorage fileStorage;
    private final ApiResponseFactory responseFactory;

    public FileController(FileStorage fileStorage, ApiResponseFactory responseFactory) {
        this.fileStorage = fileStorage;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/avatar")
    public ApiResponse<UploadAvatarResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        validateAvatar(file);
        StoredFileInfo storedFileInfo = fileStorage.saveAndGetInfo(file, "avatar");
        return responseFactory.success(new UploadAvatarResponse(
                storedFileInfo.storageKey(),
                storedFileInfo.url(),
                storedFileInfo.size()
        ));
    }

    private void validateAvatar(MultipartFile file) {
        validateImage(file, MAX_AVATAR_SIZE, "头像");
    }

    private void validateImage(MultipartFile file, long maxSize, String label) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, label + "文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, label + "大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, label + "仅支持图片格式");
        }
    }
}
