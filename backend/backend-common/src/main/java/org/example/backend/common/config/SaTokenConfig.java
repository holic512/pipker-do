package org.example.backend.common.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpInterface;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.shared.admin.mapper.AdminRoleMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.StpKit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @file SaTokenConfig
 * @project pipker-do
 * @module 共享安全 / 登录鉴权
 * @description 配置 Sa-Token API 登录过滤器、匿名接口白名单与后台角色读取。
 * @logic 1. 对 /api/** 统一执行用户或管理员登录校验；2. 放行登录、健康检查、静态文件与匿名查词接口；3. 统一输出 401 JSON。
 * @dependencies Sa-Token: SaServletFilter, Security: StpKit, Mapper: AdminRoleMapper
 * @index_tags Sa-Token, 登录鉴权, 401, 匿名接口, 角色
 * @author holic512
 */
@Configuration
public class SaTokenConfig {

    @Bean
    public SaServletFilter saServletFilter(ApiResponseFactory responseFactory) {
        return new SaServletFilter()
                .addInclude("/api/**")
                .addExclude(
                        "/api/auth/wechat/login",
                        "/api/auth/logout",
                        "/api/admin/auth/login",
                        "/api/kyyy/home/word-search",
                        "/api/kyyy/home/word-detail/**",
                        "/api/health",
                        "/static/files/**"
                )
                .setAuth(obj -> {
                    if (SaRouter.match("/api/admin/**").isHit()) {
                        StpKit.ADMIN.checkLogin();
                        return;
                    }
                    SaRouter.match("/api/**", StpKit.USER::checkLogin);
                })
                .setError(e -> {
                    SaHolder.getResponse().setStatus(401);
                    SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=UTF-8");
                    ApiResponse<Object> response = responseFactory.failure(
                            ApiResponseCode.UNAUTHORIZED,
                            "未登录或登录态已失效",
                            null
                    );
                    return "{\"code\":" + response.getCode()
                            + ",\"message\":\"" + escapeJson(response.getMessage())
                            + "\",\"data\":null"
                            + ",\"requestId\":\"" + escapeJson(response.getRequestId())
                            + "\",\"timestamp\":\"" + escapeJson(response.getTimestamp())
                            + "\"}";
                });
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Bean
    public StpInterface stpInterface(AdminRoleMapper adminRoleMapper) {
        return new StpInterface() {
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                return Collections.emptyList();
            }

            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                if (StpKit.ADMIN.getLoginType().equals(loginType)) {
                    Long adminId = Long.parseLong(String.valueOf(loginId));
                    return adminRoleMapper.selectActiveRolesByUserId(adminId).stream()
                            .map(role -> role.getRoleCode())
                            .distinct()
                            .collect(Collectors.toList());
                }
                return Collections.emptyList();
            }
        };
    }
}
