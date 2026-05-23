package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyHomeDashboardResponse;
import org.example.backend.biz.kyyy.dto.KyyyHomeDailyWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyHomeWordDetailResponse;
import org.example.backend.biz.kyyy.dto.KyyyHomeWordSearchResponse;
import org.example.backend.biz.kyyy.service.KyyyHomeDailyWordService;
import org.example.backend.biz.kyyy.service.KyyyHomeWordSearchService;
import org.example.backend.biz.kyyy.service.KyyyPracticeUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @file KyyyHomeUserController
 * @project pipker-do
 * @module 考研英语 / 小程序首页
 * @description 提供首页仪表盘、每日一词与全局查词入口的用户侧接口。
 * @logic 1. 读取用户首页仪表盘；2. 返回每日一词轮播数据；3. 查询全局有效单词列表与详情。
 * @dependencies Service: KyyyPracticeUserService, Service: KyyyHomeDailyWordService, Service: KyyyHomeWordSearchService
 * @index_tags 考研英语, 首页接口, 每日一词, 查词
 * @author holic512
 */
@RestController
@RequestMapping("/api/kyyy/home")
public class KyyyHomeUserController {

    private final KyyyPracticeUserService kyyyPracticeUserService;
    private final KyyyHomeDailyWordService kyyyHomeDailyWordService;
    private final KyyyHomeWordSearchService kyyyHomeWordSearchService;
    private final ApiResponseFactory responseFactory;

    public KyyyHomeUserController(KyyyPracticeUserService kyyyPracticeUserService,
                                  KyyyHomeDailyWordService kyyyHomeDailyWordService,
                                  KyyyHomeWordSearchService kyyyHomeWordSearchService,
                                  ApiResponseFactory responseFactory) {
        this.kyyyPracticeUserService = kyyyPracticeUserService;
        this.kyyyHomeDailyWordService = kyyyHomeDailyWordService;
        this.kyyyHomeWordSearchService = kyyyHomeWordSearchService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/dashboard")
    public ApiResponse<KyyyHomeDashboardResponse> getDashboard() {
        return responseFactory.success(kyyyPracticeUserService.getHomeDashboard(LoginUserContext.requireUserId()));
    }

    @GetMapping("/daily-words")
    public ApiResponse<List<KyyyHomeDailyWordResponse>> getDailyWords() {
        LoginUserContext.requireUserId();
        return responseFactory.success(kyyyHomeDailyWordService.getDailyWords());
    }

    @GetMapping("/word-search")
    public ApiResponse<List<KyyyHomeWordSearchResponse>> searchWords(@RequestParam(required = false) String keyword) {
        return responseFactory.success(kyyyHomeWordSearchService.searchWords(
                LoginUserContext.currentUserIdOrNull(),
                keyword
        ));
    }

    @GetMapping("/word-detail/{wordId}")
    public ApiResponse<KyyyHomeWordDetailResponse> getWordDetail(@PathVariable Long wordId) {
        return responseFactory.success(kyyyHomeWordSearchService.getWordDetail(
                LoginUserContext.currentUserIdOrNull(),
                wordId
        ));
    }
}
