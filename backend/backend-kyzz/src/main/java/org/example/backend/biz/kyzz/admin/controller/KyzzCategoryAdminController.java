package org.example.backend.biz.kyzz.admin.controller;

import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryAdminDashboardResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryAdminItemResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryAdminUpsertRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryStatusUpdateRequest;
import org.example.backend.biz.kyzz.admin.service.KyzzCategoryAdminService;
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
@RequestMapping("/api/admin/kyzz/question-bank-categories")
public class KyzzCategoryAdminController {

    private final KyzzCategoryAdminService kyzzCategoryAdminService;
    private final ApiResponseFactory responseFactory;

    public KyzzCategoryAdminController(KyzzCategoryAdminService kyzzCategoryAdminService,
                                       ApiResponseFactory responseFactory) {
        this.kyzzCategoryAdminService = kyzzCategoryAdminService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyzzCategoryAdminDashboardResponse> getDashboard(@RequestParam(required = false) String keyword,
                                                                        @RequestParam(required = false) Integer isEnabled,
                                                                        @RequestParam(required = false) Long categoryLevel) {
        return responseFactory.success(kyzzCategoryAdminService.getDashboard(
                AdminSecurityContext.requireAdminId(),
                keyword,
                isEnabled,
                categoryLevel
        ));
    }

    @PostMapping
    public ApiResponse<KyzzCategoryAdminItemResponse> createCategory(@RequestBody KyzzCategoryAdminUpsertRequest request) {
        return responseFactory.success(kyzzCategoryAdminService.createCategory(AdminSecurityContext.requireAdminId(), request));
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<KyzzCategoryAdminItemResponse> updateCategory(@PathVariable Long categoryId,
                                                                     @RequestBody KyzzCategoryAdminUpsertRequest request) {
        return responseFactory.success(kyzzCategoryAdminService.updateCategory(AdminSecurityContext.requireAdminId(), categoryId, request));
    }

    @PutMapping("/{categoryId}/status")
    public ApiResponse<KyzzCategoryAdminItemResponse> updateCategoryStatus(@PathVariable Long categoryId,
                                                                           @RequestBody KyzzCategoryStatusUpdateRequest request) {
        return responseFactory.success(kyzzCategoryAdminService.updateCategoryStatus(AdminSecurityContext.requireAdminId(), categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long categoryId) {
        kyzzCategoryAdminService.deleteCategory(AdminSecurityContext.requireAdminId(), categoryId);
        return responseFactory.success(null);
    }
}
