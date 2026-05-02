package org.example.backend.shared.security.anticrawler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * AI 索引: 登录后反扒拦截器。
 */
@Component
public class AntiCrawlerUserInterceptor implements HandlerInterceptor {

    private final AntiCrawlerRouteClassifier antiCrawlerRouteClassifier;
    private final AntiCrawlerSecurityService antiCrawlerSecurityService;

    public AntiCrawlerUserInterceptor(AntiCrawlerRouteClassifier antiCrawlerRouteClassifier,
                                      AntiCrawlerSecurityService antiCrawlerSecurityService) {
        this.antiCrawlerRouteClassifier = antiCrawlerRouteClassifier;
        this.antiCrawlerSecurityService = antiCrawlerSecurityService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AntiCrawlerRouteMatch routeMatch = antiCrawlerRouteClassifier.match(request.getRequestURI(), request.getMethod());
        if (routeMatch == null || routeMatch.group() == AntiCrawlerRouteGroup.AUTH_LOGIN) {
            return true;
        }
        antiCrawlerSecurityService.enforceAuthenticatedRequest(request, LoginUserContext.requireUserId());
        return true;
    }
}
