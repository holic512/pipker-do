package org.example.backend.shared.config.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.common.api.PageResponse;
import org.example.backend.shared.admin.support.AdminSecurityContext;
import org.example.backend.shared.config.dto.SystemConfigChangeLogResponse;
import org.example.backend.shared.config.dto.SystemConfigResponse;
import org.example.backend.shared.config.dto.SystemConfigUpdateRequest;
import org.example.backend.shared.config.service.SystemConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 索引: 后台系统配置管理接口。
 */
@RestController
@RequestMapping("/api/admin/configs")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;
    private final ApiResponseFactory responseFactory;

    public SystemConfigController(SystemConfigService systemConfigService, ApiResponseFactory responseFactory) {
        this.systemConfigService = systemConfigService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<List<SystemConfigResponse>> listConfigs(@RequestParam(required = false) String group,
                                                               @RequestParam(required = false) String keyword) {
        return responseFactory.success(systemConfigService.listConfigs(AdminSecurityContext.requireAdminId(), group, keyword));
    }

    @PutMapping("/{configKey:.+}")
    public ApiResponse<SystemConfigResponse> updateConfig(@PathVariable String configKey,
                                                          @RequestBody SystemConfigUpdateRequest request) {
        return responseFactory.success(systemConfigService.updateConfig(AdminSecurityContext.requireAdminId(), configKey, request));
    }

    @GetMapping("/change-logs")
    public ApiResponse<PageResponse<SystemConfigChangeLogResponse>> listChangeLogs(@RequestParam(required = false) String configKey,
                                                                                   @RequestParam(required = false) Long pageNo,
                                                                                   @RequestParam(required = false) Long pageSize) {
        return responseFactory.success(systemConfigService.listChangeLogs(AdminSecurityContext.requireAdminId(), configKey, pageNo, pageSize));
    }
}
