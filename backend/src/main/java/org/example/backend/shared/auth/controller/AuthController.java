package org.example.backend.shared.auth.controller;

import org.example.backend.shared.auth.dto.LoginResponse;
import org.example.backend.shared.auth.dto.WechatLoginRequest;
import org.example.backend.shared.auth.service.AuthService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.shared.security.StpKit;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ApiResponseFactory responseFactory;

    public AuthController(AuthService authService, ApiResponseFactory responseFactory) {
        this.authService = authService;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/wechat/login")
    public ApiResponse<LoginResponse> wechatLogin(@RequestBody WechatLoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "code 不能为空");
        }
        return responseFactory.success(authService.loginByWechatCode(request.getCode()));
    }

    @PostMapping("/logout")
    public ApiResponse<Object> logout() {
        if (StpKit.USER.isLogin()) {
            authService.logout();
        }
        return responseFactory.success("退出登录成功", null);
    }
}
