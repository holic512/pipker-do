package org.example.backend.shared.security.anticrawler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * AI 索引: 登录前反扒过滤器。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class AntiCrawlerLoginFilter extends OncePerRequestFilter {

    private final AntiCrawlerRouteClassifier antiCrawlerRouteClassifier;
    private final AntiCrawlerSecurityService antiCrawlerSecurityService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public AntiCrawlerLoginFilter(AntiCrawlerRouteClassifier antiCrawlerRouteClassifier,
                                  AntiCrawlerSecurityService antiCrawlerSecurityService,
                                  HandlerExceptionResolver handlerExceptionResolver) {
        this.antiCrawlerRouteClassifier = antiCrawlerRouteClassifier;
        this.antiCrawlerSecurityService = antiCrawlerSecurityService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        AntiCrawlerRouteMatch routeMatch = antiCrawlerRouteClassifier.match(request.getRequestURI(), request.getMethod());
        return routeMatch == null || routeMatch.group() != AntiCrawlerRouteGroup.AUTH_LOGIN;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            antiCrawlerSecurityService.enforceLoginRequest(request);
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
