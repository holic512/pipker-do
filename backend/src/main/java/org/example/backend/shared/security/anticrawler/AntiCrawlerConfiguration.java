package org.example.backend.shared.security.anticrawler;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * AI 索引: 反扒模块基础配置。
 */
@Configuration
@EnableConfigurationProperties(AntiCrawlerProperties.class)
public class AntiCrawlerConfiguration {

    @Bean
    public Clock antiCrawlerClock() {
        return Clock.systemDefaultZone();
    }
}
