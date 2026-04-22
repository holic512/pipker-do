package org.example.backend.common.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpInterface;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.util.satoken.StpKit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class SaTokenConfig {

    @Bean
    public SaServletFilter saServletFilter(ApiResponseFactory responseFactory) {
        return new SaServletFilter()
                .addInclude("/api/**")
                .addExclude(
                        "/api/auth/wechat/login",
                        "/api/auth/logout",
                        "/api/health",
                        "/static/files/**"
                )
                .setAuth(obj -> SaRouter.match("/api/**", StpKit.USER::checkLogin))
                .setError(e -> {
                    SaHolder.getResponse().setStatus(401);
                    SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=UTF-8");
                    return responseFactory.failure(ApiResponseCode.UNAUTHORIZED, "未登录或登录态已失效", null);
                });
    }

    @Bean
    public StpInterface stpInterface() {
        return new StpInterface() {
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                return Collections.emptyList();
            }

            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                return Collections.emptyList();
            }
        };
    }
}
