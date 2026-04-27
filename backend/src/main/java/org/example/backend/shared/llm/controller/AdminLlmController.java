package org.example.backend.shared.llm.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.common.api.PageResponse;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.service.AdminPermissionService;
import org.example.backend.shared.admin.support.AdminSecurityContext;
import org.example.backend.shared.llm.dto.AdminLlmTestRequest;
import org.example.backend.shared.llm.dto.AdminLlmTestResponse;
import org.example.backend.shared.llm.dto.LlmCallRecordResponse;
import org.example.backend.shared.llm.dto.LlmGenerateResult;
import org.example.backend.shared.llm.service.LlmService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: 后台 LLM 测试调用与记录接口。
 */
@RestController
@RequestMapping("/api/admin/llm")
public class AdminLlmController {

    private final LlmService llmService;
    private final AdminPermissionService adminPermissionService;
    private final ApiResponseFactory responseFactory;

    public AdminLlmController(LlmService llmService,
                              AdminPermissionService adminPermissionService,
                              ApiResponseFactory responseFactory) {
        this.llmService = llmService;
        this.adminPermissionService = adminPermissionService;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/test")
    public ApiResponse<AdminLlmTestResponse> test(@RequestBody AdminLlmTestRequest request) {
        Long operatorId = AdminSecurityContext.requireAdminId();
        adminPermissionService.requireSystemManager(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "测试调用参数不能为空");
        }
        String mode = request == null || !StringUtils.hasText(request.getMode()) ? "text" : request.getMode().trim();
        LlmGenerateResult result = "json".equalsIgnoreCase(mode)
                ? llmService.generateJson(operatorId, request.getScene(), request.getSystemPrompt(), request.getUserPrompt(), request.getSchemaName(), request.getJsonSchema())
                : llmService.generateText(operatorId, request.getScene(), request.getSystemPrompt(), request.getUserPrompt());
        return responseFactory.success(new AdminLlmTestResponse(mode.toLowerCase(), result));
    }

    @GetMapping("/records")
    public ApiResponse<PageResponse<LlmCallRecordResponse>> listRecords(@RequestParam(required = false) String status,
                                                                        @RequestParam(required = false) String scene,
                                                                        @RequestParam(required = false) Long pageNo,
                                                                        @RequestParam(required = false) Long pageSize) {
        Long operatorId = AdminSecurityContext.requireAdminId();
        adminPermissionService.requireSystemManager(operatorId);
        return responseFactory.success(llmService.listRecords(operatorId, status, scene, pageNo, pageSize));
    }

    @GetMapping("/records/{id}")
    public ApiResponse<LlmCallRecordResponse> getRecordDetail(@PathVariable Long id) {
        Long operatorId = AdminSecurityContext.requireAdminId();
        adminPermissionService.requireSystemManager(operatorId);
        return responseFactory.success(llmService.getRecordDetail(operatorId, id));
    }
}
