package org.example.backend.biz.kyzz.controller;

import org.example.backend.biz.kyzz.dto.KyzzFavoriteQuestionResponse;
import org.example.backend.biz.kyzz.dto.KyzzFavoriteQuestionToggleResponse;
import org.example.backend.biz.kyzz.service.KyzzFavoriteQuestionUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: KYZZ 用户侧题目收藏控制器。
 */
@RestController
@RequestMapping("/api/kyzz/favorite-questions")
public class KyzzFavoriteQuestionUserController {

    private final KyzzFavoriteQuestionUserService kyzzFavoriteQuestionUserService;
    private final ApiResponseFactory responseFactory;

    public KyzzFavoriteQuestionUserController(KyzzFavoriteQuestionUserService kyzzFavoriteQuestionUserService,
                                              ApiResponseFactory responseFactory) {
        this.kyzzFavoriteQuestionUserService = kyzzFavoriteQuestionUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyzzFavoriteQuestionResponse> getFavoriteQuestions(@RequestParam(required = false) String keyword) {
        return responseFactory.success(kyzzFavoriteQuestionUserService.getFavoriteQuestions(
                LoginUserContext.requireUserId(),
                keyword
        ));
    }

    @PutMapping("/{questionId}")
    public ApiResponse<KyzzFavoriteQuestionToggleResponse> favoriteQuestion(@PathVariable Long questionId) {
        return responseFactory.success(kyzzFavoriteQuestionUserService.favoriteQuestion(
                LoginUserContext.requireUserId(),
                questionId
        ));
    }

    @DeleteMapping("/{questionId}")
    public ApiResponse<KyzzFavoriteQuestionToggleResponse> unfavoriteQuestion(@PathVariable Long questionId) {
        return responseFactory.success(kyzzFavoriteQuestionUserService.unfavoriteQuestion(
                LoginUserContext.requireUserId(),
                questionId
        ));
    }
}
