package org.example.backend.biz.kyzz.controller;

import org.example.backend.biz.kyzz.dto.KyzzQuestionCommentCreateRequest;
import org.example.backend.biz.kyzz.dto.KyzzQuestionCommentItemResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionCommentLikeToggleResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionCommentPageResponse;
import org.example.backend.biz.kyzz.service.KyzzQuestionCommentUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: KYZZ 用户侧题目评论控制器。
 */
@RestController
@RequestMapping("/api/kyzz/comments")
public class KyzzQuestionCommentUserController {

    private final KyzzQuestionCommentUserService kyzzQuestionCommentUserService;
    private final ApiResponseFactory responseFactory;

    public KyzzQuestionCommentUserController(KyzzQuestionCommentUserService kyzzQuestionCommentUserService,
                                             ApiResponseFactory responseFactory) {
        this.kyzzQuestionCommentUserService = kyzzQuestionCommentUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/questions/{questionId}")
    public ApiResponse<KyzzQuestionCommentPageResponse> getQuestionComments(@PathVariable Long questionId,
                                                                            @RequestParam(required = false) Long pageNo,
                                                                            @RequestParam(required = false) Long pageSize) {
        return responseFactory.success(kyzzQuestionCommentUserService.getQuestionComments(
                LoginUserContext.requireUserId(),
                questionId,
                pageNo,
                pageSize
        ));
    }

    @PostMapping("/questions/{questionId}")
    public ApiResponse<KyzzQuestionCommentItemResponse> createQuestionComment(@PathVariable Long questionId,
                                                                              @RequestBody KyzzQuestionCommentCreateRequest request) {
        return responseFactory.success(kyzzQuestionCommentUserService.createQuestionComment(
                LoginUserContext.requireUserId(),
                questionId,
                request
        ));
    }

    @PutMapping("/{commentId}/like")
    public ApiResponse<KyzzQuestionCommentLikeToggleResponse> likeQuestionComment(@PathVariable Long commentId) {
        return responseFactory.success(kyzzQuestionCommentUserService.likeQuestionComment(
                LoginUserContext.requireUserId(),
                commentId
        ));
    }

    @DeleteMapping("/{commentId}/like")
    public ApiResponse<KyzzQuestionCommentLikeToggleResponse> unlikeQuestionComment(@PathVariable Long commentId) {
        return responseFactory.success(kyzzQuestionCommentUserService.unlikeQuestionComment(
                LoginUserContext.requireUserId(),
                commentId
        ));
    }
}
