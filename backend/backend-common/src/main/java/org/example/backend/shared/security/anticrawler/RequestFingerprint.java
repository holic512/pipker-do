package org.example.backend.shared.security.anticrawler;

import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * AI 索引: 反扒请求指纹快照。
 */
public record RequestFingerprint(
        String requestId,
        String path,
        String method,
        String clientIp,
        String deviceId,
        String userAgent,
        String referer,
        String clientPlatform,
        String clientVersion,
        String tokenHash
) {

    public boolean hasDeviceId() {
        return StringUtils.hasText(deviceId);
    }

    public boolean matchesWechatClient() {
        String resolvedUserAgent = userAgent == null ? "" : userAgent.toLowerCase(Locale.ROOT);
        String resolvedPlatform = clientPlatform == null ? "" : clientPlatform.toLowerCase(Locale.ROOT);
        return resolvedUserAgent.contains("micromessenger")
                || resolvedPlatform.contains("mp-weixin")
                || resolvedPlatform.contains("wechat");
    }
}
