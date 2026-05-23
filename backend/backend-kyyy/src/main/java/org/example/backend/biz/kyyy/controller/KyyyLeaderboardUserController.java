/**
 * @file KyyyLeaderboardUserController
 * @project pipker-do
 * @module 考研英语 / 排行榜
 * @description 对外提供英语用户侧排行榜查询接口。
 * @logic 1. 从登录态提取当前用户；2. 接收榜单周期与数量限制；3. 返回综合背词与阅读排行榜数据。
 * @dependencies Service: KyyyLeaderboardUserService, API: /api/kyyy/leaderboard
 * @index_tags 考研英语, 排行榜接口, 用户侧Controller, 综合榜
 * @author holic512
 */
package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyLeaderboardResponse;
import org.example.backend.biz.kyyy.service.KyyyLeaderboardUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kyyy/leaderboard")
public class KyyyLeaderboardUserController {

    private final KyyyLeaderboardUserService kyyyLeaderboardUserService;
    private final ApiResponseFactory responseFactory;

    public KyyyLeaderboardUserController(KyyyLeaderboardUserService kyyyLeaderboardUserService,
                                         ApiResponseFactory responseFactory) {
        this.kyyyLeaderboardUserService = kyyyLeaderboardUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyyyLeaderboardResponse> getLeaderboard(@RequestParam(required = false) String scope,
                                                               @RequestParam(required = false) Integer limit) {
        return responseFactory.success(kyyyLeaderboardUserService.getLeaderboard(
                LoginUserContext.requireUserId(),
                scope,
                limit
        ));
    }
}
