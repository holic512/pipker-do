/**
 * @file LocalFileProxyConfiguration
 * @project pipker-do
 * @module 共享存储 / 本地文件代理配置
 * @description 在本地存储模式下按 storage.local.public-base-url 注册文件代理处理器。
 * @logic 1. 从 public-base-url 生成代理匹配规则；2. 注册 SimpleUrlHandlerMapping；3. COS 模式不注册本地代理。
 * @dependencies LocalStorageProperties, LocalFileProxyHandler, SimpleUrlHandlerMapping
 * @index_tags 本地文件代理, HandlerMapping, public-base-url, storage.local
 * @author holic512
 */
package org.example.backend.shared.storage.driver.local;

import org.example.backend.shared.storage.config.LocalStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileProxyConfiguration {

    @Bean
    public SimpleUrlHandlerMapping localFileProxyHandlerMapping(LocalStorageProperties properties,
                                                               LocalFileProxyHandler handler) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of(LocalFileProxyPath.toHandlerPattern(properties.getPublicBaseUrl()), handler));
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return mapping;
    }
}
