/**
 * @file KyyyReadingUserController
 * @project pipker-do
 * @module 考研英语 / 阅读做题
 * @description 提供阅读会话整篇加载、单题存稿与交卷提交接口。
 * @logic 1. 读取或创建当前阅读会话；2. 保存单题答案草稿；3. 提交整篇并回显解析。
 * @dependencies Service: KyyyReadingUserService, Security: LoginUserContext
 * @index_tags 考研英语, 阅读接口, 会话加载, 交卷提交
 * @author holic512
 */
package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyReadingAnswerDraftRequest;
import org.example.backend.biz.kyyy.dto.KyyyReadingSessionResponse;
import org.example.backend.biz.kyyy.service.KyyyReadingUserService;
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
@RequestMapping("/api/kyyy/reading")
public class KyyyReadingUserController {

    private final KyyyReadingUserService kyyyReadingUserService;
    private final ApiResponseFactory responseFactory;

    public KyyyReadingUserController(KyyyReadingUserService kyyyReadingUserService,
                                     ApiResponseFactory responseFactory) {
        this.kyyyReadingUserService = kyyyReadingUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/session")
    public ApiResponse<KyyyReadingSessionResponse> getSession(@RequestParam(required = false) Long passageId,
                                                              @RequestParam(required = false) Boolean freshAttempt) {
        return responseFactory.success(kyyyReadingUserService.getSession(
                LoginUserContext.requireUserId(),
                passageId,
                freshAttempt
        ));
    }

    @PutMapping("/session/{sessionId}/answers/{questionId}")
    public ApiResponse<KyyyReadingSessionResponse> saveAnswerDraft(@PathVariable Long sessionId,
                                                                   @PathVariable Long questionId,
                                                                   @RequestBody KyyyReadingAnswerDraftRequest request) {
        return responseFactory.success(kyyyReadingUserService.saveAnswerDraft(
                LoginUserContext.requireUserId(),
                sessionId,
                questionId,
                request
        ));
    }

    @PostMapping("/session/{sessionId}/submit")
    public ApiResponse<KyyyReadingSessionResponse> submitSession(@PathVariable Long sessionId) {
        return responseFactory.success(kyyyReadingUserService.submitSession(
                LoginUserContext.requireUserId(),
                sessionId
        ));
    }
}
