/**
 * @file KyyyTranslationUserController
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 提供翻译知识库首页总览、分页列表与详情查询接口。
 * @logic 1. 返回方向、模式、年份与推荐真题；2. 支持多条件分页查询；3. 按翻译题 ID 返回详情与分段。
 * @dependencies Service: KyyyTranslationUserService, API: /api/kyyy/translation
 * @index_tags 考研英语, 翻译接口, 知识库, 列表, 详情
 * @author holic512
 */
package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyTranslationDetailResponse;
import org.example.backend.biz.kyyy.dto.KyyyTranslationListResponse;
import org.example.backend.biz.kyyy.dto.KyyyTranslationOverviewResponse;
import org.example.backend.biz.kyyy.service.KyyyTranslationUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kyyy/translation")
public class KyyyTranslationUserController {

    private final KyyyTranslationUserService translationUserService;
    private final ApiResponseFactory responseFactory;

    public KyyyTranslationUserController(KyyyTranslationUserService translationUserService,
                                         ApiResponseFactory responseFactory) {
        this.translationUserService = translationUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/overview")
    public ApiResponse<KyyyTranslationOverviewResponse> getOverview() {
        return responseFactory.success(translationUserService.getOverview());
    }

    @GetMapping("/passages")
    public ApiResponse<KyyyTranslationListResponse> getPassages(@RequestParam(required = false) String examDirection,
                                                                @RequestParam(required = false) String translationMode,
                                                                @RequestParam(required = false) Integer sourceYear,
                                                                @RequestParam(required = false) String keyword,
                                                                @RequestParam(required = false) Long pageNo,
                                                                @RequestParam(required = false) Long pageSize) {
        return responseFactory.success(translationUserService.getPassages(
                examDirection,
                translationMode,
                sourceYear,
                keyword,
                pageNo,
                pageSize
        ));
    }

    @GetMapping("/passages/{passageId}")
    public ApiResponse<KyyyTranslationDetailResponse> getPassageDetail(@PathVariable Long passageId) {
        return responseFactory.success(translationUserService.getPassageDetail(passageId));
    }
}
