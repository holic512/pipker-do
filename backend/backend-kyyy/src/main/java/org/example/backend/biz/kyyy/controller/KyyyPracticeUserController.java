package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyPracticeNextWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingRequest;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingResponse;
import org.example.backend.biz.kyyy.service.KyyyPracticeUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: KYYY 用户侧刷题控制器。
 */
@RestController
@RequestMapping("/api/kyyy/practice")
public class KyyyPracticeUserController {

    private final KyyyPracticeUserService kyyyPracticeUserService;
    private final ApiResponseFactory responseFactory;

    public KyyyPracticeUserController(KyyyPracticeUserService kyyyPracticeUserService,
                                      ApiResponseFactory responseFactory) {
        this.kyyyPracticeUserService = kyyyPracticeUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/settings")
    public ApiResponse<KyyyPracticeSettingResponse> getSettings() {
        return responseFactory.success(kyyyPracticeUserService.getSettings(LoginUserContext.requireUserId()));
    }

    @GetMapping("/next-word")
    public ApiResponse<KyyyPracticeNextWordResponse> getNextWord() {
        return responseFactory.success(kyyyPracticeUserService.getNextWord(LoginUserContext.requireUserId()));
    }

    @PutMapping("/settings")
    public ApiResponse<KyyyPracticeSettingResponse> updateSettings(@RequestBody KyyyPracticeSettingRequest request) {
        return responseFactory.success(kyyyPracticeUserService.updateSettings(
                LoginUserContext.requireUserId(),
                request
        ));
    }
}
