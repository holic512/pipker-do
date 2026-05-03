package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyHomeDashboardResponse;
import org.example.backend.biz.kyyy.dto.KyyyHomeDailyWordResponse;
import org.example.backend.biz.kyyy.service.KyyyHomeDailyWordService;
import org.example.backend.biz.kyyy.service.KyyyPracticeUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 索引: KYYY 用户侧首页控制器。
 */
@RestController
@RequestMapping("/api/kyyy/home")
public class KyyyHomeUserController {

    private final KyyyPracticeUserService kyyyPracticeUserService;
    private final KyyyHomeDailyWordService kyyyHomeDailyWordService;
    private final ApiResponseFactory responseFactory;

    public KyyyHomeUserController(KyyyPracticeUserService kyyyPracticeUserService,
                                  KyyyHomeDailyWordService kyyyHomeDailyWordService,
                                  ApiResponseFactory responseFactory) {
        this.kyyyPracticeUserService = kyyyPracticeUserService;
        this.kyyyHomeDailyWordService = kyyyHomeDailyWordService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/dashboard")
    public ApiResponse<KyyyHomeDashboardResponse> getDashboard() {
        return responseFactory.success(kyyyPracticeUserService.getHomeDashboard(LoginUserContext.requireUserId()));
    }

    @GetMapping("/daily-words")
    public ApiResponse<List<KyyyHomeDailyWordResponse>> getDailyWords() {
        LoginUserContext.requireUserId();
        return responseFactory.success(kyyyHomeDailyWordService.getDailyWords());
    }
}
