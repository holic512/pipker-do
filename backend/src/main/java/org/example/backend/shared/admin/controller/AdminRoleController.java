package org.example.backend.shared.admin.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.admin.dto.AdminRoleStatusUpdateRequest;
import org.example.backend.shared.admin.dto.AdminRoleSummaryResponse;
import org.example.backend.shared.admin.dto.AdminRoleUpsertRequest;
import org.example.backend.shared.admin.service.AdminRoleManagementService;
import org.example.backend.shared.admin.support.AdminSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

    private final AdminRoleManagementService adminRoleManagementService;
    private final ApiResponseFactory responseFactory;

    public AdminRoleController(AdminRoleManagementService adminRoleManagementService, ApiResponseFactory responseFactory) {
        this.adminRoleManagementService = adminRoleManagementService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<List<AdminRoleSummaryResponse>> listRoles(@RequestParam(required = false) String keyword,
                                                                 @RequestParam(required = false) Integer status) {
        return responseFactory.success(adminRoleManagementService.listRoles(AdminSecurityContext.requireAdminId(), keyword, status));
    }

    @PostMapping
    public ApiResponse<AdminRoleSummaryResponse> createRole(@RequestBody AdminRoleUpsertRequest request) {
        return responseFactory.success(adminRoleManagementService.createRole(AdminSecurityContext.requireAdminId(), request));
    }

    @PutMapping("/{roleId}")
    public ApiResponse<AdminRoleSummaryResponse> updateRole(@PathVariable Long roleId,
                                                            @RequestBody AdminRoleUpsertRequest request) {
        return responseFactory.success(adminRoleManagementService.updateRole(AdminSecurityContext.requireAdminId(), roleId, request));
    }

    @PutMapping("/{roleId}/status")
    public ApiResponse<AdminRoleSummaryResponse> updateRoleStatus(@PathVariable Long roleId,
                                                                  @RequestBody AdminRoleStatusUpdateRequest request) {
        return responseFactory.success(adminRoleManagementService.updateRoleStatus(AdminSecurityContext.requireAdminId(), roleId, request));
    }
}
