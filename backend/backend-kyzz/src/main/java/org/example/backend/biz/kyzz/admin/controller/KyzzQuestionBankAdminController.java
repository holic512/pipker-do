package org.example.backend.biz.kyzz.admin.controller;

import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankAdminDashboardResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankAdminItemResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankCoverUpdateRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankAdminUpsertRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankStatusUpdateRequest;
import org.example.backend.biz.kyzz.admin.service.KyzzQuestionBankAdminService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.admin.support.AdminSecurityContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/kyzz/question-banks")
public class KyzzQuestionBankAdminController {

    private final KyzzQuestionBankAdminService kyzzQuestionBankAdminService;
    private final ApiResponseFactory responseFactory;

    public KyzzQuestionBankAdminController(KyzzQuestionBankAdminService kyzzQuestionBankAdminService,
                                           ApiResponseFactory responseFactory) {
        this.kyzzQuestionBankAdminService = kyzzQuestionBankAdminService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyzzQuestionBankAdminDashboardResponse> getDashboard(@RequestParam(required = false) String keyword,
                                                                            @RequestParam(required = false) Integer status,
                                                                            @RequestParam(required = false) Long categoryId,
                                                                            @RequestParam(required = false) Integer difficultyLevel) {
        return responseFactory.success(kyzzQuestionBankAdminService.getDashboard(
                AdminSecurityContext.requireAdminId(),
                keyword,
                status,
                categoryId,
                difficultyLevel
        ));
    }

    @PostMapping
    public ApiResponse<KyzzQuestionBankAdminItemResponse> createQuestionBank(@RequestBody KyzzQuestionBankAdminUpsertRequest request) {
        return responseFactory.success(kyzzQuestionBankAdminService.createQuestionBank(AdminSecurityContext.requireAdminId(), request));
    }

    @PutMapping("/{bankId}")
    public ApiResponse<KyzzQuestionBankAdminItemResponse> updateQuestionBank(@PathVariable Long bankId,
                                                                             @RequestBody KyzzQuestionBankAdminUpsertRequest request) {
        return responseFactory.success(kyzzQuestionBankAdminService.updateQuestionBank(AdminSecurityContext.requireAdminId(), bankId, request));
    }

    @PutMapping("/{bankId}/status")
    public ApiResponse<KyzzQuestionBankAdminItemResponse> updateQuestionBankStatus(@PathVariable Long bankId,
                                                                                   @RequestBody KyzzQuestionBankStatusUpdateRequest request) {
        return responseFactory.success(kyzzQuestionBankAdminService.updateQuestionBankStatus(AdminSecurityContext.requireAdminId(), bankId, request));
    }

    @PutMapping("/{bankId}/cover")
    public ApiResponse<KyzzQuestionBankAdminItemResponse> updateQuestionBankCover(@PathVariable Long bankId,
                                                                                  @RequestBody KyzzQuestionBankCoverUpdateRequest request) {
        return responseFactory.success(kyzzQuestionBankAdminService.updateQuestionBankCover(AdminSecurityContext.requireAdminId(), bankId, request));
    }

    @PutMapping("/{bankId}/sync-question-count")
    public ApiResponse<KyzzQuestionBankAdminItemResponse> syncQuestionCount(@PathVariable Long bankId) {
        return responseFactory.success(kyzzQuestionBankAdminService.syncQuestionCount(AdminSecurityContext.requireAdminId(), bankId));
    }

    @DeleteMapping("/{bankId}")
    public ApiResponse<Void> deleteQuestionBank(@PathVariable Long bankId) {
        kyzzQuestionBankAdminService.deleteQuestionBank(AdminSecurityContext.requireAdminId(), bankId);
        return responseFactory.success(null);
    }
}
