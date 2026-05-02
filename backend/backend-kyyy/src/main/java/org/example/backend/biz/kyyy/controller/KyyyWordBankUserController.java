package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyWordBankListResponse;
import org.example.backend.biz.kyyy.dto.KyyyWordBankRecordResponse;
import org.example.backend.biz.kyyy.dto.KyyyWordBankSelectionRequest;
import org.example.backend.biz.kyyy.service.KyyyWordBankUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: KYYY 用户侧词库控制器。
 */
@RestController
@RequestMapping("/api/kyyy/word-banks")
public class KyyyWordBankUserController {

    private final KyyyWordBankUserService kyyyWordBankUserService;
    private final ApiResponseFactory responseFactory;

    public KyyyWordBankUserController(KyyyWordBankUserService kyyyWordBankUserService,
                                      ApiResponseFactory responseFactory) {
        this.kyyyWordBankUserService = kyyyWordBankUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyyyWordBankListResponse> getWordBanks() {
        return responseFactory.success(kyyyWordBankUserService.getWordBanks(LoginUserContext.requireUserId()));
    }

    @PutMapping("/{bankId}/selection")
    public ApiResponse<KyyyWordBankRecordResponse> updateSelection(@PathVariable Long bankId,
                                                                   @RequestBody KyyyWordBankSelectionRequest request) {
        return responseFactory.success(kyyyWordBankUserService.updateWordBankSelection(
                LoginUserContext.requireUserId(),
                bankId,
                request
        ));
    }
}
