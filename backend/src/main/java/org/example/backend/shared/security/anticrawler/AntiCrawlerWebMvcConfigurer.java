package org.example.backend.shared.security.anticrawler;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * AI 索引: 反扒登录后拦截器注册。
 */
@Configuration
public class AntiCrawlerWebMvcConfigurer implements WebMvcConfigurer {

    private final AntiCrawlerUserInterceptor antiCrawlerUserInterceptor;

    public AntiCrawlerWebMvcConfigurer(AntiCrawlerUserInterceptor antiCrawlerUserInterceptor) {
        this.antiCrawlerUserInterceptor = antiCrawlerUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(antiCrawlerUserInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/admin/**", "/api/health", "/static/files/**");
    }
}
