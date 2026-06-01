/**
 * @file CosStorageProperties
 * @project pipker-do
 * @module 共享存储 / 腾讯云 COS 配置
 * @description 读取腾讯云 COS 存储连接、桶和私有签名 URL 配置。
 * @logic 1. 绑定 storage.cos 配置；2. 提供 COSClient 初始化参数；3. 控制私有读签名 URL 过期时间。
 * @dependencies Tencent COS SDK: COSClient, Spring Boot ConfigurationProperties
 * @index_tags 腾讯云COS, 对象存储, 私有桶, 签名URL, storage.cos
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
@ConfigurationProperties(prefix = "storage.cos")
public class CosStorageProperties {

    private String secretId;

    private String secretKey;

    private String region;

    private String bucket;

    /**
     * 预留给公开读或 CDN 域名模式；私有桶签名 URL 默认由 COS SDK 生成。
     */
    private String publicBaseUrl;

    private Long signedUrlExpireSeconds = 1800L;

    private Integer connectionTimeoutMs = 30000;

    private Integer socketTimeoutMs = 30000;

    private Integer maxConnections = 1024;
}
