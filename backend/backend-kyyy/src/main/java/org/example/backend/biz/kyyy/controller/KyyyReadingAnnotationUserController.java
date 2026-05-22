/**
 * @file KyyyReadingAnnotationUserController
 * @project pipker-do
 * @module 考研英语 / 阅读标注
 * @description 提供阅读正文与题干标注的新建、备注修改和删除接口。
 * @logic 1. 新建字符区间标注；2. 修改既有标注备注；3. 逻辑删除标注记录。
 * @dependencies Service: KyyyReadingAnnotationUserService, Security: LoginUserContext
 * @index_tags 考研英语, 阅读标注接口, 高亮备注, 标注管理
 * @author holic512
 */
package org.example.backend.biz.kyyy.controller;

import org.example.backend.biz.kyyy.dto.KyyyReadingAnnotationCreateRequest;
import org.example.backend.biz.kyyy.dto.KyyyReadingAnnotationResponse;
import org.example.backend.biz.kyyy.dto.KyyyReadingAnnotationUpdateRequest;
import org.example.backend.biz.kyyy.service.KyyyReadingAnnotationUserService;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kyyy/reading/annotations")
public class KyyyReadingAnnotationUserController {

    private final KyyyReadingAnnotationUserService kyyyReadingAnnotationUserService;
    private final ApiResponseFactory responseFactory;

    public KyyyReadingAnnotationUserController(KyyyReadingAnnotationUserService kyyyReadingAnnotationUserService,
                                               ApiResponseFactory responseFactory) {
        this.kyyyReadingAnnotationUserService = kyyyReadingAnnotationUserService;
        this.responseFactory = responseFactory;
    }

    @PostMapping
    public ApiResponse<KyyyReadingAnnotationResponse> createAnnotation(@RequestBody KyyyReadingAnnotationCreateRequest request) {
        return responseFactory.success(kyyyReadingAnnotationUserService.createAnnotation(
                LoginUserContext.requireUserId(),
                request
        ));
    }

    @PutMapping("/{annotationId}")
    public ApiResponse<KyyyReadingAnnotationResponse> updateAnnotation(@PathVariable Long annotationId,
                                                                       @RequestBody KyyyReadingAnnotationUpdateRequest request) {
        return responseFactory.success(kyyyReadingAnnotationUserService.updateAnnotation(
                LoginUserContext.requireUserId(),
                annotationId,
                request
        ));
    }

    @DeleteMapping("/{annotationId}")
    public ApiResponse<Void> deleteAnnotation(@PathVariable Long annotationId) {
        kyyyReadingAnnotationUserService.deleteAnnotation(LoginUserContext.requireUserId(), annotationId);
        return responseFactory.success(null);
    }
}
