/**
 * @file KyyyWritingUserController
 * @project pipker-do
 * @module 考研英语 / 作文知识库
 * @description 提供作文知识库首页总览、分页列表与详情查询接口。
 * @logic 1. 返回总览分面与推荐真题；2. 支持多条件分页查询；3. 按作文 ID 返回详情。
 * @dependencies Service: KyyyWritingUserService, API: /api/kyyy/writing
 * @index_tags 考研英语, 作文接口, 知识库, 列表, 详情
 * @author holic512
 */
package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyWritingEssayDetailResponse;
import org.example.backend.biz.kyyy.dto.KyyyWritingEssayListResponse;
import org.example.backend.biz.kyyy.dto.KyyyWritingOverviewResponse;
import org.example.backend.biz.kyyy.service.KyyyWritingUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kyyy/writing")
public class KyyyWritingUserController {

    private final KyyyWritingUserService kyyyWritingUserService;
    private final ApiResponseFactory responseFactory;

    public KyyyWritingUserController(KyyyWritingUserService kyyyWritingUserService,
                                     ApiResponseFactory responseFactory) {
        this.kyyyWritingUserService = kyyyWritingUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/overview")
    public ApiResponse<KyyyWritingOverviewResponse> getOverview() {
        return responseFactory.success(kyyyWritingUserService.getOverview());
    }

    @GetMapping("/essays")
    public ApiResponse<KyyyWritingEssayListResponse> getEssays(@RequestParam(required = false) String examDirection,
                                                               @RequestParam(required = false) String essaySection,
                                                               @RequestParam(required = false) String promptCategory,
                                                               @RequestParam(required = false) Integer sourceYear,
                                                               @RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false) Long pageNo,
                                                               @RequestParam(required = false) Long pageSize) {
        return responseFactory.success(kyyyWritingUserService.getEssays(
                examDirection,
                essaySection,
                promptCategory,
                sourceYear,
                keyword,
                pageNo,
                pageSize
        ));
    }

    @GetMapping("/essays/{essayId}")
    public ApiResponse<KyyyWritingEssayDetailResponse> getEssayDetail(@PathVariable Long essayId) {
        return responseFactory.success(kyyyWritingUserService.getEssayDetail(essayId));
    }
}
