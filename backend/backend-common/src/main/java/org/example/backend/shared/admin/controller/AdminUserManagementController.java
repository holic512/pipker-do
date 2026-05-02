package org.example.backend.shared.admin.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.admin.dto.AdminProjectResponse;
import org.example.backend.shared.admin.dto.AdminUserBasicUpdateRequest;
import org.example.backend.shared.admin.dto.AdminUserCreateRequest;
import org.example.backend.shared.admin.dto.AdminUserPasswordResetRequest;
import org.example.backend.shared.admin.dto.AdminUserProjectAssignmentRequest;
import org.example.backend.shared.admin.dto.AdminUserRoleAssignmentRequest;
import org.example.backend.shared.admin.dto.AdminUserStatusUpdateRequest;
import org.example.backend.shared.admin.dto.AdminUserSummaryResponse;
import org.example.backend.shared.admin.service.AdminUserManagementService;
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
@RequestMapping("/api/admin/users")
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;
    private final ApiResponseFactory responseFactory;

    public AdminUserManagementController(AdminUserManagementService adminUserManagementService,
                                         ApiResponseFactory responseFactory) {
        this.adminUserManagementService = adminUserManagementService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<List<AdminUserSummaryResponse>> listAdmins(@RequestParam(required = false) String keyword,
                                                                  @RequestParam(required = false) Integer status,
                                                                  @RequestParam(required = false) String roleCode,
                                                                  @RequestParam(required = false) String projectCode) {
        return responseFactory.success(adminUserManagementService.listAdmins(
                AdminSecurityContext.requireAdminId(), keyword, status, roleCode, projectCode
        ));
    }

    @GetMapping("/{userId}")
    public ApiResponse<AdminUserSummaryResponse> getAdminDetail(@PathVariable Long userId) {
        return responseFactory.success(adminUserManagementService.getAdminDetail(AdminSecurityContext.requireAdminId(), userId));
    }

    @GetMapping("/project-catalog")
    public ApiResponse<List<AdminProjectResponse>> listProjectCatalog() {
        return responseFactory.success(adminUserManagementService.listProjectCatalog(AdminSecurityContext.requireAdminId()));
    }

    @PostMapping
    public ApiResponse<AdminUserSummaryResponse> createAdmin(@RequestBody AdminUserCreateRequest request) {
        return responseFactory.success(adminUserManagementService.createAdmin(AdminSecurityContext.requireAdminId(), request));
    }

    @PutMapping("/{userId}")
    public ApiResponse<AdminUserSummaryResponse> updateBasicInfo(@PathVariable Long userId,
                                                                 @RequestBody AdminUserBasicUpdateRequest request) {
        return responseFactory.success(adminUserManagementService.updateBasicInfo(AdminSecurityContext.requireAdminId(), userId, request));
    }

    @PutMapping("/{userId}/status")
    public ApiResponse<AdminUserSummaryResponse> updateStatus(@PathVariable Long userId,
                                                              @RequestBody AdminUserStatusUpdateRequest request) {
        return responseFactory.success(adminUserManagementService.updateStatus(AdminSecurityContext.requireAdminId(), userId, request));
    }

    @PutMapping("/{userId}/roles")
    public ApiResponse<AdminUserSummaryResponse> updateRoles(@PathVariable Long userId,
                                                             @RequestBody AdminUserRoleAssignmentRequest request) {
        return responseFactory.success(adminUserManagementService.updateRoles(AdminSecurityContext.requireAdminId(), userId, request));
    }

    @PutMapping("/{userId}/projects")
    public ApiResponse<AdminUserSummaryResponse> updateProjects(@PathVariable Long userId,
                                                                @RequestBody AdminUserProjectAssignmentRequest request) {
        return responseFactory.success(adminUserManagementService.updateProjects(AdminSecurityContext.requireAdminId(), userId, request));
    }

    @PutMapping("/{userId}/password/reset")
    public ApiResponse<Object> resetPassword(@PathVariable Long userId,
                                             @RequestBody AdminUserPasswordResetRequest request) {
        adminUserManagementService.resetPassword(AdminSecurityContext.requireAdminId(), userId, request);
        return responseFactory.success("密码重置成功", null);
    }
}
