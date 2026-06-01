/**
 * @file LocalStorageProperties
 * @project pipker-do
 * @module 共享存储 / 本地存储配置
 * @description 绑定本地文件根目录、代理 URL、临时目录、hash 分片和业务目录白名单。
 * @logic 1. 读取 storage.local 配置；2. 兼容 root-dir 旧配置；3. 为本地和 COS 上传预处理提供公共路径参数。
 * @dependencies Spring Boot ConfigurationProperties
 * @index_tags 本地文件配置, public-base-url, base-path, allowed-biz-types
 * @author holic512
 */
package org.example.backend.shared.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "storage.local")
public class LocalStorageProperties {

    /**
     * 文件在服务器上的根目录
     * 例如：D:/flower-files 或 /data/flower-files
     */
    private String basePath;

    /**
     * @deprecated 兼容旧配置，优先使用 basePath
     */
    @Deprecated
    private String rootDir;

    /**
     * 对外访问的基础 URL 前缀
     * 例如：http://localhost:8080/files/
     * 或 Nginx 反向代理后的 https://cdn.xxx.com/files/
     */
    private String publicBaseUrl;

    /**
     * 临时文件目录。相对路径时会拼接到 basePath 下。
     */
    private String tempDir = ".tmp";

    /**
     * 哈希算法
     */
    private String hashAlgorithm = "SHA-256";

    /**
     * 目录分片层级
     */
    private Integer shardDepth = 2;

    /**
     * 每级分片宽度
     */
    private Integer shardWidth = 2;

    /**
     * 允许的业务目录；为空时表示不限制
     */
    private List<String> allowedBizTypes = new ArrayList<>();

    /**
     * 生成文件名时是否保留原始扩展名
     */
    private Boolean keepExtension = true;

    /**
     * @deprecated 日期目录已废弃，保留字段仅为兼容旧配置
     */
    @Deprecated
    private Boolean enableDatePath = true;

    public String getBasePath() {
        return basePath != null && !basePath.isBlank() ? basePath : rootDir;
    }
}
