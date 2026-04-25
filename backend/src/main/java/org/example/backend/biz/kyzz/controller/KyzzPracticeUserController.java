package org.example.backend.biz.kyzz.controller;

import org.example.backend.biz.kyzz.dto.KyzzPracticeAnswerPreviewResponse;
import org.example.backend.biz.kyzz.dto.KyzzPracticeDashboardResponse;
import org.example.backend.biz.kyzz.dto.KyzzPracticeReviewRequest;
import org.example.backend.biz.kyzz.dto.KyzzPracticeReviewResponse;
import org.example.backend.biz.kyzz.dto.KyzzPracticeSettingRequest;
import org.example.backend.biz.kyzz.dto.KyzzPracticeSettingResponse;
import org.example.backend.biz.kyzz.dto.KyzzPracticeSelfJudgementRequest;
import org.example.backend.biz.kyzz.dto.KyzzPracticeSessionResponse;
import org.example.backend.biz.kyzz.service.KyzzPracticeUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: KYZZ 用户侧刷题控制器。
 */
@RestController
@RequestMapping("/api/kyzz/practice")
public class KyzzPracticeUserController {

    private final KyzzPracticeUserService kyzzPracticeUserService;
    private final ApiResponseFactory responseFactory;

    public KyzzPracticeUserController(KyzzPracticeUserService kyzzPracticeUserService,
                                      ApiResponseFactory responseFactory) {
        this.kyzzPracticeUserService = kyzzPracticeUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/dashboard")
    public ApiResponse<KyzzPracticeDashboardResponse> getDashboard() {
        return responseFactory.success(kyzzPracticeUserService.getDashboard(LoginUserContext.requireUserId()));
    }

    @GetMapping("/session")
    public ApiResponse<KyzzPracticeSessionResponse> getSession(@RequestParam(required = false) Long bankId,
                                                               @RequestParam(required = false) Long questionId,
                                                               @RequestParam(required = false) Boolean freshAttempt,
                                                               @RequestParam(required = false) String sourceType,
                                                               @RequestParam(required = false) String sourceStatus,
                                                               @RequestParam(required = false) String keyword) {
        return responseFactory.success(kyzzPracticeUserService.getSession(
                LoginUserContext.requireUserId(),
                bankId,
                questionId,
                freshAttempt,
                sourceType,
                sourceStatus,
                keyword
        ));
    }

    @GetMapping("/settings")
    public ApiResponse<KyzzPracticeSettingResponse> getSettings() {
        return responseFactory.success(kyzzPracticeUserService.getSettings(LoginUserContext.requireUserId()));
    }

    @PutMapping("/settings")
    public ApiResponse<KyzzPracticeSettingResponse> updateSettings(@RequestBody KyzzPracticeSettingRequest request) {
        return responseFactory.success(kyzzPracticeUserService.updateSettings(
                LoginUserContext.requireUserId(),
                request
        ));
    }

    @GetMapping("/questions/{questionId}/answer-preview")
    public ApiResponse<KyzzPracticeAnswerPreviewResponse> getAnswerPreview(@PathVariable Long questionId,
                                                                           @RequestParam Long bankId) {
        return responseFactory.success(kyzzPracticeUserService.getAnswerPreview(
                LoginUserContext.requireUserId(),
                questionId,
                bankId
        ));
    }

    @PostMapping("/questions/{questionId}/review")
    public ApiResponse<KyzzPracticeReviewResponse> reviewQuestion(@PathVariable Long questionId,
                                                                  @RequestBody KyzzPracticeReviewRequest request) {
        return responseFactory.success(kyzzPracticeUserService.reviewQuestion(
                LoginUserContext.requireUserId(),
                questionId,
                request
        ));
    }

    @PostMapping("/questions/{questionId}/self-judgement")
    public ApiResponse<KyzzPracticeReviewResponse> selfJudgeShortQuestion(@PathVariable Long questionId,
                                                                          @RequestBody KyzzPracticeSelfJudgementRequest request) {
        return responseFactory.success(kyzzPracticeUserService.selfJudgeShortQuestion(
                LoginUserContext.requireUserId(),
                questionId,
                request
        ));
    }
}
