/**
 * @file KyyyWrongQuestionUserController
 * @project pipker-do
 * @module 考研英语 / 阅读错题本
 * @description 提供用户侧阅读错题本列表查询接口。
 * @logic 1. 接收状态和关键词筛选；2. 读取当前登录用户；3. 返回阅读错题本汇总与列表。
 * @dependencies Service: KyyyWrongQuestionUserService, Security: LoginUserContext
 * @index_tags 考研英语, 阅读错题本接口, 用户侧查询, 错题列表
 * @author holic512
 */
package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyWrongQuestionResponse;
import org.example.backend.biz.kyyy.service.KyyyWrongQuestionUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kyyy/wrong-questions")
public class KyyyWrongQuestionUserController {

    private final KyyyWrongQuestionUserService kyyyWrongQuestionUserService;
    private final ApiResponseFactory responseFactory;

    public KyyyWrongQuestionUserController(KyyyWrongQuestionUserService kyyyWrongQuestionUserService,
                                           ApiResponseFactory responseFactory) {
        this.kyyyWrongQuestionUserService = kyyyWrongQuestionUserService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<KyyyWrongQuestionResponse> getWrongQuestions(@RequestParam(required = false) String status,
                                                                    @RequestParam(required = false) String keyword) {
        return responseFactory.success(kyyyWrongQuestionUserService.getWrongQuestions(
                LoginUserContext.requireUserId(),
                status,
                keyword
        ));
    }
}
