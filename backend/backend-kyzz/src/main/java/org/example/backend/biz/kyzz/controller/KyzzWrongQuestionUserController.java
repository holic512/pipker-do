package org.example.backend.biz.kyzz.controller;

import org.example.backend.biz.kyzz.dto.KyzzWrongQuestionResponse;
import org.example.backend.biz.kyzz.service.KyzzWrongQuestionUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: KYZZ 用户侧错题本控制器。
 */
@RestController
@RequestMapping("/api/kyzz/wrong-questions")
public class KyzzWrongQuestionUserController {

    private final KyzzWrongQuestionUserService kyzzWrongQuestionUserService;
    private final ApiResponseFactory responseFactory;

    public KyzzWrongQuestionUserController(KyzzWrongQuestionUserService kyzzWrongQuestionUserService,
                                           ApiResponseFactory responseFactory) {
        this.kyzzWrongQuestionUserService = kyzzWrongQuestionUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyzzWrongQuestionResponse> getWrongQuestions(@RequestParam(required = false) String status,
                                                                    @RequestParam(required = false) String keyword) {
        return responseFactory.success(kyzzWrongQuestionUserService.getWrongQuestions(
                LoginUserContext.requireUserId(),
                status,
                keyword
        ));
    }
}
