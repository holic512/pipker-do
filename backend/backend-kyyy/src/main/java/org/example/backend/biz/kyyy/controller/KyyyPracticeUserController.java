/**
 * @file KyyyPracticeUserController
 * @project pipker-do
 * @module 考研英语 / 用户侧背词
 * @description 提供默认词库学习会话、学习反馈与背词设置接口。
 * @logic 1. 读取或创建学习/复习会话；2. 提交当前单词反馈并推进队列；3. 提供设置与兼容 next-word 接口。
 * @dependencies Service: KyyyPracticeUserService, Security: LoginUserContext
 * @index_tags 考研英语, 背词接口, 学习会话, 复习接口
 * @author holic512
 */
package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyPracticeFeedbackRequest;
import org.example.backend.biz.kyyy.dto.KyyyPracticeNextWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingRequest;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSessionResponse;
import org.example.backend.biz.kyyy.service.KyyyPracticeUserService;
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

    @GetMapping("/session")
    public ApiResponse<KyyyPracticeSessionResponse> getSession(@RequestParam(required = false) String mode,
                                                               @RequestParam(required = false) Boolean freshAttempt) {
        return responseFactory.success(kyyyPracticeUserService.getSession(
                LoginUserContext.requireUserId(),
                mode,
                freshAttempt
        ));
    }

    @GetMapping("/next-word")
    public ApiResponse<KyyyPracticeNextWordResponse> getNextWord() {
        return responseFactory.success(kyyyPracticeUserService.getNextWord(LoginUserContext.requireUserId()));
    }

    @PostMapping("/session/{sessionId}/feedback")
    public ApiResponse<KyyyPracticeSessionResponse> submitFeedback(@PathVariable Long sessionId,
                                                                   @RequestBody KyyyPracticeFeedbackRequest request) {
        return responseFactory.success(kyyyPracticeUserService.submitFeedback(
                LoginUserContext.requireUserId(),
                sessionId,
                request
        ));
    }

    @PutMapping("/settings")
    public ApiResponse<KyyyPracticeSettingResponse> updateSettings(@RequestBody KyyyPracticeSettingRequest request) {
        return responseFactory.success(kyyyPracticeUserService.updateSettings(
                LoginUserContext.requireUserId(),
                request
        ));
    }
}
