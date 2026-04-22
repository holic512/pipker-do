package org.example.backend.shared.storage.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.storage.dto.UploadAvatarResponse;
import org.example.backend.shared.storage.service.LocalFileStorage;
import org.example.backend.shared.storage.service.StoredFileInfo;
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

    private final LocalFileStorage localFileStorage;
    private final ApiResponseFactory responseFactory;

    public FileController(LocalFileStorage localFileStorage, ApiResponseFactory responseFactory) {
        this.localFileStorage = localFileStorage;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/avatar")
    public ApiResponse<UploadAvatarResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        validateAvatar(file);
        StoredFileInfo storedFileInfo = localFileStorage.saveAndGetInfo(file, "avatar");
        return responseFactory.success(new UploadAvatarResponse(
                storedFileInfo.storageKey(),
                storedFileInfo.url(),
                storedFileInfo.size()
        ));
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "头像文件不能为空");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "头像大小不能超过2MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "仅支持图片格式头像");
        }
    }
}
