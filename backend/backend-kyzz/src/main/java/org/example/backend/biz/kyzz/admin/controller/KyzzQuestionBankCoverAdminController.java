/**
 * @file KyzzQuestionBankCoverAdminController
 * @project pipker-do
 * @module 考研政治 / 管理端题库封面上传
 * @description 提供政治题库封面图片上传接口，返回共享文件服务生成的系统 key 和访问 URL。
 * @logic 1. 校验管理员 kyzz 项目权限；2. 校验封面大小与图片类型；3. 调用 FileStorage 保存到当前存储驱动。
 * @dependencies FileStorage, KyzzAdminAccessSupport, ApiResponseFactory
 * @index_tags 考研政治, 题库封面, 文件上传, FileStorage, pipker#
 * @author holic512
 */
package org.example.backend.biz.kyzz.admin.controller;

import org.example.backend.biz.kyzz.admin.support.KyzzAdminAccessSupport;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.support.AdminSecurityContext;
import org.example.backend.shared.storage.core.FileStorage;
import org.example.backend.shared.storage.core.StoredFileInfo;
import org.example.backend.shared.storage.dto.UploadFileResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/kyzz/question-banks")
public class KyzzQuestionBankCoverAdminController {

    private static final long MAX_QUESTION_BANK_COVER_SIZE = 5L * 1024 * 1024;
    private static final String COVER_BIZ_TYPE = "kyzz-question-bank-cover";

    private final FileStorage fileStorage;
    private final KyzzAdminAccessSupport kyzzAdminAccessSupport;
    private final ApiResponseFactory responseFactory;

    public KyzzQuestionBankCoverAdminController(FileStorage fileStorage,
                                                KyzzAdminAccessSupport kyzzAdminAccessSupport,
                                                ApiResponseFactory responseFactory) {
        this.fileStorage = fileStorage;
        this.kyzzAdminAccessSupport = kyzzAdminAccessSupport;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/covers/upload")
    public ApiResponse<UploadFileResponse> uploadCover(@RequestParam("file") MultipartFile file) {
        kyzzAdminAccessSupport.requireProjectAccess(AdminSecurityContext.requireAdminId());
        validateImage(file, MAX_QUESTION_BANK_COVER_SIZE, "题库封面");
        StoredFileInfo storedFileInfo = fileStorage.saveAndGetInfo(file, COVER_BIZ_TYPE);
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
