package org.example.backend.shared.admin.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.dto.AdminCurrentUserResponse;
import org.example.backend.shared.admin.dto.AdminLoginRequest;
import org.example.backend.shared.admin.dto.AdminLoginResponse;
import org.example.backend.shared.admin.dto.AdminPasswordUpdateRequest;
import org.example.backend.shared.admin.dto.AdminProjectResponse;
import org.example.backend.shared.admin.dto.AdminProfileUpdateRequest;
import org.example.backend.shared.admin.service.AdminAuthService;
import org.example.backend.shared.admin.support.AdminSecurityContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final ApiResponseFactory responseFactory;

    public AdminAuthController(AdminAuthService adminAuthService, ApiResponseFactory responseFactory) {
        this.adminAuthService = adminAuthService;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "用户名和密码不能为空");
        }
        return responseFactory.success(adminAuthService.login(request.getUsername().trim(), request.getPassword()));
    }

    @PostMapping("/logout")
    public ApiResponse<Object> logout() {
        adminAuthService.logout();
        return responseFactory.success("退出登录成功", null);
    }

    @GetMapping("/me")
    public ApiResponse<AdminCurrentUserResponse> me() {
        return responseFactory.success(adminAuthService.buildCurrentAdmin(AdminSecurityContext.requireAdminId()));
    }

    @GetMapping("/projects")
    public ApiResponse<List<AdminProjectResponse>> projects() {
        return responseFactory.success(adminAuthService.getAvailableProjects(AdminSecurityContext.requireAdminId()));
    }

    @PutMapping("/profile")
    public ApiResponse<AdminCurrentUserResponse> updateProfile(@RequestBody AdminProfileUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "资料参数不能为空");
        }
        return responseFactory.success(adminAuthService.updateProfile(AdminSecurityContext.requireAdminId(), request));
    }

    @PutMapping("/password")
    public ApiResponse<Object> updatePassword(@RequestBody AdminPasswordUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "密码参数不能为空");
        }
        adminAuthService.updatePassword(AdminSecurityContext.requireAdminId(), request);
        return responseFactory.success("密码修改成功", null);
    }
}
