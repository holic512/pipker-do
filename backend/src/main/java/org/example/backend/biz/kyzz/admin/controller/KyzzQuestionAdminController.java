package org.example.backend.biz.kyzz.admin.controller;

import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminDashboardResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminDetailResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminItemResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminUpsertRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionStatusUpdateRequest;
import org.example.backend.biz.kyzz.admin.service.KyzzQuestionAdminService;
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

/**
 * AI 索引: KYZZ 题目管理后台接口。
 */
@RestController
@RequestMapping("/api/admin/kyzz/questions")
public class KyzzQuestionAdminController {

    private final KyzzQuestionAdminService kyzzQuestionAdminService;
    private final ApiResponseFactory responseFactory;

    public KyzzQuestionAdminController(KyzzQuestionAdminService kyzzQuestionAdminService,
                                       ApiResponseFactory responseFactory) {
        this.kyzzQuestionAdminService = kyzzQuestionAdminService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyzzQuestionAdminDashboardResponse> getDashboard(@RequestParam(defaultValue = "1") Long pageNo,
                                                                        @RequestParam(defaultValue = "20") Long pageSize,
                                                                        @RequestParam(required = false) String keyword,
                                                                        @RequestParam(required = false) Long questionBankId,
                                                                        @RequestParam(required = false) Long categoryId,
                                                                        @RequestParam(required = false) Long tagId,
                                                                        @RequestParam(required = false) String questionType,
                                                                        @RequestParam(required = false) Integer status,
                                                                        @RequestParam(required = false) Integer difficultyLevel,
                                                                        @RequestParam(required = false) Integer yearNo) {
        return responseFactory.success(kyzzQuestionAdminService.getDashboard(
                AdminSecurityContext.requireAdminId(),
                pageNo,
                pageSize,
                keyword,
                questionBankId,
                categoryId,
                tagId,
                questionType,
                status,
                difficultyLevel,
                yearNo
        ));
    }

    @GetMapping("/{questionId}")
    public ApiResponse<KyzzQuestionAdminDetailResponse> getQuestionDetail(@PathVariable Long questionId) {
        return responseFactory.success(kyzzQuestionAdminService.getQuestionDetail(AdminSecurityContext.requireAdminId(), questionId));
    }

    @PostMapping
    public ApiResponse<KyzzQuestionAdminDetailResponse> createQuestion(@RequestBody KyzzQuestionAdminUpsertRequest request) {
        return responseFactory.success(kyzzQuestionAdminService.createQuestion(AdminSecurityContext.requireAdminId(), request));
    }

    @PutMapping("/{questionId}")
    public ApiResponse<KyzzQuestionAdminDetailResponse> updateQuestion(@PathVariable Long questionId,
                                                                       @RequestBody KyzzQuestionAdminUpsertRequest request) {
        return responseFactory.success(kyzzQuestionAdminService.updateQuestion(AdminSecurityContext.requireAdminId(), questionId, request));
    }

    @PutMapping("/{questionId}/status")
    public ApiResponse<KyzzQuestionAdminItemResponse> updateQuestionStatus(@PathVariable Long questionId,
                                                                           @RequestBody KyzzQuestionStatusUpdateRequest request) {
        return responseFactory.success(kyzzQuestionAdminService.updateQuestionStatus(AdminSecurityContext.requireAdminId(), questionId, request));
    }

    @DeleteMapping("/{questionId}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long questionId) {
        kyzzQuestionAdminService.deleteQuestion(AdminSecurityContext.requireAdminId(), questionId);
        return responseFactory.success(null);
    }
}
