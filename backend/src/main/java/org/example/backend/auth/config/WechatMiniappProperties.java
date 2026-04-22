package org.example.backend.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatMiniappProperties {

    private String appId;

    private String appSecret;
}
