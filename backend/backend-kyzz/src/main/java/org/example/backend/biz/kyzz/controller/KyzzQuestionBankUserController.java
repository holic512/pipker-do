package org.example.backend.biz.kyzz.controller;

import org.example.backend.biz.kyzz.dto.KyzzQuestionBankMineResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankPublicRecordResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankPublicResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankSelectionRequest;
import org.example.backend.biz.kyzz.service.KyzzQuestionBankUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: KYZZ 用户侧题库控制器。
 */
@RestController
@RequestMapping("/api/kyzz/question-banks")
public class KyzzQuestionBankUserController {

    private final KyzzQuestionBankUserService kyzzQuestionBankUserService;
    private final ApiResponseFactory responseFactory;

    public KyzzQuestionBankUserController(KyzzQuestionBankUserService kyzzQuestionBankUserService,
                                          ApiResponseFactory responseFactory) {
        this.kyzzQuestionBankUserService = kyzzQuestionBankUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/mine")
    public ApiResponse<KyzzQuestionBankMineResponse> getMineQuestionBanks() {
        return responseFactory.success(kyzzQuestionBankUserService.getMineQuestionBanks(LoginUserContext.requireUserId()));
    }

    @GetMapping("/public")
    public ApiResponse<KyzzQuestionBankPublicResponse> getPublicQuestionBanks(@RequestParam(required = false) String keyword,
                                                                              @RequestParam(required = false) Long categoryId,
                                                                              @RequestParam(required = false) Integer difficultyLevel,
                                                                              @RequestParam(required = false) String selectionStatus) {
        return responseFactory.success(kyzzQuestionBankUserService.getPublicQuestionBanks(
                LoginUserContext.requireUserId(),
                keyword,
                categoryId,
                difficultyLevel,
                selectionStatus
        ));
    }

    @PutMapping("/{bankId}/selection")
    public ApiResponse<KyzzQuestionBankPublicRecordResponse> updateSelection(@PathVariable Long bankId,
                                                                             @RequestBody KyzzQuestionBankSelectionRequest request) {
        return responseFactory.success(kyzzQuestionBankUserService.updateQuestionBankSelection(
                LoginUserContext.requireUserId(),
                bankId,
                request
        ));
    }
}
