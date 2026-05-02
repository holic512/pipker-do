package org.example.backend.biz.kyzz.admin.controller;

import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankAdminDashboardResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankAdminUserDetailResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankSelectionUpdateRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankSelectionUpdateResponse;
import org.example.backend.biz.kyzz.admin.service.KyzzUserQuestionBankAdminService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.admin.support.AdminSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: KYZZ 管理端用户题库选择控制器。
 */
@RestController
@RequestMapping("/api/admin/kyzz/user-question-banks")
public class KyzzUserQuestionBankAdminController {

    private final KyzzUserQuestionBankAdminService kyzzUserQuestionBankAdminService;
    private final ApiResponseFactory responseFactory;

    public KyzzUserQuestionBankAdminController(KyzzUserQuestionBankAdminService kyzzUserQuestionBankAdminService,
                                               ApiResponseFactory responseFactory) {
        this.kyzzUserQuestionBankAdminService = kyzzUserQuestionBankAdminService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyzzUserQuestionBankAdminDashboardResponse> getDashboard(@RequestParam(required = false) String keyword,
                                                                                 @RequestParam(required = false) String selectionStatus,
                                                                                 @RequestParam(required = false) Long pageNo,
                                                                                 @RequestParam(required = false) Long pageSize) {
        return responseFactory.success(kyzzUserQuestionBankAdminService.getDashboard(
                AdminSecurityContext.requireAdminId(),
                keyword,
                selectionStatus,
                pageNo,
                pageSize
        ));
    }

    @GetMapping("/{userId}")
    public ApiResponse<KyzzUserQuestionBankAdminUserDetailResponse> getUserDetail(@PathVariable Long userId) {
        return responseFactory.success(kyzzUserQuestionBankAdminService.getUserDetail(
                AdminSecurityContext.requireAdminId(),
                userId
        ));
    }

    @PutMapping("/{userId}/banks/{bankId}/selection")
    public ApiResponse<KyzzUserQuestionBankSelectionUpdateResponse> updateSelection(@PathVariable Long userId,
                                                                                     @PathVariable Long bankId,
                                                                                     @RequestBody KyzzUserQuestionBankSelectionUpdateRequest request) {
        return responseFactory.success(kyzzUserQuestionBankAdminService.updateSelection(
                AdminSecurityContext.requireAdminId(),
                userId,
                bankId,
                request
        ));
    }
}
