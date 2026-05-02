package org.example.backend.biz.kyzz.admin.controller;

import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionTagAdminDashboardResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionTagAdminItemResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionTagAdminUpsertRequest;
import org.example.backend.biz.kyzz.admin.service.KyzzQuestionTagAdminService;
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
 * AI 索引: KYZZ 题目标签后台接口。
 */
@RestController
@RequestMapping("/api/admin/kyzz/question-tags")
public class KyzzQuestionTagAdminController {

    private final KyzzQuestionTagAdminService kyzzQuestionTagAdminService;
    private final ApiResponseFactory responseFactory;

    public KyzzQuestionTagAdminController(KyzzQuestionTagAdminService kyzzQuestionTagAdminService,
                                          ApiResponseFactory responseFactory) {
        this.kyzzQuestionTagAdminService = kyzzQuestionTagAdminService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyzzQuestionTagAdminDashboardResponse> getDashboard(@RequestParam(required = false) String keyword,
                                                                           @RequestParam(required = false) Integer usedStatus) {
        return responseFactory.success(kyzzQuestionTagAdminService.getDashboard(
                AdminSecurityContext.requireAdminId(),
                keyword,
                usedStatus
        ));
    }

    @PostMapping
    public ApiResponse<KyzzQuestionTagAdminItemResponse> createQuestionTag(@RequestBody KyzzQuestionTagAdminUpsertRequest request) {
        return responseFactory.success(kyzzQuestionTagAdminService.createQuestionTag(AdminSecurityContext.requireAdminId(), request));
    }

    @PutMapping("/{tagId}")
    public ApiResponse<KyzzQuestionTagAdminItemResponse> updateQuestionTag(@PathVariable Long tagId,
                                                                           @RequestBody KyzzQuestionTagAdminUpsertRequest request) {
        return responseFactory.success(kyzzQuestionTagAdminService.updateQuestionTag(AdminSecurityContext.requireAdminId(), tagId, request));
    }

    @PutMapping("/{tagId}/sync-use-count")
    public ApiResponse<KyzzQuestionTagAdminItemResponse> syncUseCount(@PathVariable Long tagId) {
        return responseFactory.success(kyzzQuestionTagAdminService.syncUseCount(AdminSecurityContext.requireAdminId(), tagId));
    }

    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> deleteQuestionTag(@PathVariable Long tagId) {
        kyzzQuestionTagAdminService.deleteQuestionTag(AdminSecurityContext.requireAdminId(), tagId);
        return responseFactory.success(null);
    }
}
