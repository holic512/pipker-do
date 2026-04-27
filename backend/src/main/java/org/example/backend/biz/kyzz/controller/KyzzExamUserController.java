package org.example.backend.biz.kyzz.controller;

import org.example.backend.biz.kyzz.dto.KyzzExamAnswerSaveRequest;
import org.example.backend.biz.kyzz.dto.KyzzExamAnswerSaveResponse;
import org.example.backend.biz.kyzz.dto.KyzzExamDetailResponse;
import org.example.backend.biz.kyzz.dto.KyzzExamEntryResponse;
import org.example.backend.biz.kyzz.dto.KyzzExamStartRequest;
import org.example.backend.biz.kyzz.dto.KyzzExamSummaryResponse;
import org.example.backend.biz.kyzz.service.KyzzExamUserService;
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

import java.util.List;

/**
 * AI 索引: KYZZ 用户侧 VIP 考试控制器。
 */
@RestController
@RequestMapping("/api/kyzz/exam")
public class KyzzExamUserController {

    private final KyzzExamUserService kyzzExamUserService;
    private final ApiResponseFactory responseFactory;

    public KyzzExamUserController(KyzzExamUserService kyzzExamUserService,
                                  ApiResponseFactory responseFactory) {
        this.kyzzExamUserService = kyzzExamUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/entry")
    public ApiResponse<KyzzExamEntryResponse> getEntry() {
        return responseFactory.success(kyzzExamUserService.getEntry(LoginUserContext.requireUserId()));
    }

    @GetMapping("/history")
    public ApiResponse<List<KyzzExamSummaryResponse>> listHistory(@RequestParam(required = false) Integer limit) {
        return responseFactory.success(kyzzExamUserService.listHistory(LoginUserContext.requireUserId(), limit));
    }

    @PostMapping("/sessions")
    public ApiResponse<KyzzExamDetailResponse> startExam(@RequestBody KyzzExamStartRequest request) {
        return responseFactory.success(kyzzExamUserService.startExam(LoginUserContext.requireUserId(), request));
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<KyzzExamDetailResponse> getExamDetail(@PathVariable Long sessionId) {
        return responseFactory.success(kyzzExamUserService.getExamDetail(LoginUserContext.requireUserId(), sessionId));
    }

    @PutMapping("/sessions/{sessionId}/questions/{questionId}/answer")
    public ApiResponse<KyzzExamAnswerSaveResponse> saveAnswer(@PathVariable Long sessionId,
                                                              @PathVariable Long questionId,
                                                              @RequestBody KyzzExamAnswerSaveRequest request) {
        return responseFactory.success(kyzzExamUserService.saveAnswer(
                LoginUserContext.requireUserId(),
                sessionId,
                questionId,
                request
        ));
    }

    @PostMapping("/sessions/{sessionId}/submit")
    public ApiResponse<KyzzExamSummaryResponse> submitExam(@PathVariable Long sessionId) {
        return responseFactory.success(kyzzExamUserService.submitExam(LoginUserContext.requireUserId(), sessionId));
    }
}
