/**
 * @file CosStorageConfiguration
 * @project pipker-do
 * @module 共享存储 / 腾讯云 COS 客户端配置
 * @description 在 storage.type=cos 时创建腾讯云 COSClient Bean。
 * @logic 1. 校验 COS 必填配置；2. 按 region 和超时参数初始化 ClientConfig；3. 使用 destroyMethod 关闭 COSClient。
 * @dependencies Tencent COS SDK: COSClient, CosStorageProperties
 * @index_tags 腾讯云COS, COSClient, 存储配置, Spring Bean
 * @author holic512
 */
package org.example.backend.shared.storage.driver.cos;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import org.example.backend.shared.storage.config.CosStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "cos")
public class CosStorageConfiguration {

    @Bean(destroyMethod = "shutdown")
    public COSClient cosClient(CosStorageProperties properties) {
        COSCredentials credentials = new BasicCOSCredentials(
                requireNonEmpty(properties.getSecretId(), "请在配置中设置 storage.cos.secret-id"),
                requireNonEmpty(properties.getSecretKey(), "请在配置中设置 storage.cos.secret-key")
        );
        ClientConfig clientConfig = new ClientConfig(new Region(
                requireNonEmpty(properties.getRegion(), "请在配置中设置 storage.cos.region")
        ));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        clientConfig.setConnectionTimeout(defaultIfInvalid(properties.getConnectionTimeoutMs(), 30000));
        clientConfig.setSocketTimeout(defaultIfInvalid(properties.getSocketTimeoutMs(), 30000));
        clientConfig.setMaxConnectionsCount(defaultIfInvalid(properties.getMaxConnections(), 1024));
        return new COSClient(credentials, clientConfig);
    }

    private String requireNonEmpty(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
        return value;
    }

    private int defaultIfInvalid(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }
}
