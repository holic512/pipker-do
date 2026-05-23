/**
 * @file KyyyFavoriteWordUserController
 * @project pipker-do
 * @module 考研英语 / 单词收藏
 * @description 提供用户侧单词收藏列表查询与收藏开关接口。
 * @logic 1. 读取当前登录用户收藏列表；2. 支持按 wordId 收藏；3. 支持取消收藏并返回最新状态。
 * @dependencies Service: KyyyFavoriteWordUserService, Security: LoginUserContext
 * @index_tags 考研英语, 单词收藏接口, 用户侧查询, 收藏开关
 * @author holic512
 */
package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyFavoriteWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyFavoriteWordToggleResponse;
import org.example.backend.biz.kyyy.service.KyyyFavoriteWordUserService;
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

@RestController
@RequestMapping("/api/kyyy/favorite-words")
public class KyyyFavoriteWordUserController {

    private final KyyyFavoriteWordUserService kyyyFavoriteWordUserService;
    private final ApiResponseFactory responseFactory;

    public KyyyFavoriteWordUserController(KyyyFavoriteWordUserService kyyyFavoriteWordUserService,
                                          ApiResponseFactory responseFactory) {
        this.kyyyFavoriteWordUserService = kyyyFavoriteWordUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyyyFavoriteWordResponse> getFavoriteWords(@RequestParam(required = false) String keyword) {
        return responseFactory.success(kyyyFavoriteWordUserService.getFavoriteWords(
                LoginUserContext.requireUserId(),
                keyword
        ));
    }

    @PutMapping("/{wordId}")
    public ApiResponse<KyyyFavoriteWordToggleResponse> favoriteWord(@PathVariable Long wordId) {
        return responseFactory.success(kyyyFavoriteWordUserService.favoriteWord(
                LoginUserContext.requireUserId(),
                wordId
        ));
    }

    @DeleteMapping("/{wordId}")
    public ApiResponse<KyyyFavoriteWordToggleResponse> unfavoriteWord(@PathVariable Long wordId) {
        return responseFactory.success(kyyyFavoriteWordUserService.unfavoriteWord(
                LoginUserContext.requireUserId(),
                wordId
        ));
    }
}
