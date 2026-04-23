package org.example.backend.biz.kyzz.controller;

import org.example.backend.biz.kyzz.dto.KyzzLeaderboardResponse;
import org.example.backend.biz.kyzz.service.KyzzLeaderboardUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: KYZZ 用户侧排行榜控制器。
 */
@RestController
@RequestMapping("/api/kyzz/leaderboard")
public class KyzzLeaderboardUserController {

    private final KyzzLeaderboardUserService kyzzLeaderboardUserService;
    private final ApiResponseFactory responseFactory;

    public KyzzLeaderboardUserController(KyzzLeaderboardUserService kyzzLeaderboardUserService,
                                         ApiResponseFactory responseFactory) {
        this.kyzzLeaderboardUserService = kyzzLeaderboardUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyzzLeaderboardResponse> getLeaderboard(@RequestParam(required = false) String scope,
                                                               @RequestParam(required = false) Integer limit) {
        return responseFactory.success(kyzzLeaderboardUserService.getLeaderboard(
                LoginUserContext.requireUserId(),
                scope,
                limit
        ));
    }
}
