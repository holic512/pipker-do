package org.example.backend.shared.storage.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.storage.dto.UploadFileResponse;
import org.example.backend.shared.storage.service.LocalFileStorage;
import org.example.backend.shared.storage.service.StoredFileInfo;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/files")
public class AdminFileController {

    private static final long MAX_QUESTION_BANK_COVER_SIZE = 5L * 1024 * 1024;

    private final LocalFileStorage localFileStorage;
    private final ApiResponseFactory responseFactory;

    public AdminFileController(LocalFileStorage localFileStorage, ApiResponseFactory responseFactory) {
        this.localFileStorage = localFileStorage;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/kyzz-question-bank-cover")
    public ApiResponse<UploadFileResponse> uploadKyzzQuestionBankCover(@RequestParam("file") MultipartFile file) {
        validateImage(file, MAX_QUESTION_BANK_COVER_SIZE, "题库封面");
        StoredFileInfo storedFileInfo = localFileStorage.saveAndGetInfo(file, "kyzz-question-bank-cover");
        return responseFactory.success(new UploadFileResponse(
                storedFileInfo.storageKey(),
                storedFileInfo.url(),
                storedFileInfo.size(),
                storedFileInfo.contentType(),
                storedFileInfo.originalFilename()
        ));
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
