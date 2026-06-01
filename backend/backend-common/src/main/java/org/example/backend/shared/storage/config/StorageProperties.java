/**
 * @file StorageProperties
 * @project pipker-do
 * @module 共享存储 / 存储驱动配置
 * @description 读取全局存储驱动类型，用于在本地存储和腾讯云 COS 存储之间切换。
 * @logic 1. 绑定 storage.type；2. 默认使用 local 保持原有本地文件存储行为。
 * @dependencies Spring Boot ConfigurationProperties
 * @index_tags 文件存储, 驱动切换, storage.type, local, cos
 * @author holic512
 */
package org.example.backend.shared.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * 存储驱动类型：local 或 cos
     */
    private String type = "local";
}
