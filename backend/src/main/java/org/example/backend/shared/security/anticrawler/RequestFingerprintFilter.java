package org.example.backend.shared.security.anticrawler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * AI 索引: 反扒请求指纹采集过滤器。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestFingerprintFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        RequestFingerprintHolder.set(buildFingerprint(request));
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestFingerprintHolder.clear();
        }
    }

    private RequestFingerprint buildFingerprint(HttpServletRequest request) {
        return new RequestFingerprint(
                request.getHeader(AntiCrawlerHeaderNames.REQUEST_ID),
                request.getRequestURI(),
                request.getMethod(),
                resolveClientIp(request),
                trimToNull(request.getHeader(AntiCrawlerHeaderNames.DEVICE_ID)),
                trimToNull(request.getHeader("User-Agent")),
                trimToNull(request.getHeader("Referer")),
                trimToNull(request.getHeader(AntiCrawlerHeaderNames.CLIENT_PLATFORM)),
                trimToNull(request.getHeader(AntiCrawlerHeaderNames.CLIENT_VERSION)),
                hashToken(request.getHeader("Authorization"))
        );
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = trimToNull(request.getHeader("X-Forwarded-For"));
        if (forwardedFor != null) {
            String[] values = forwardedFor.split(",");
            for (String value : values) {
                String candidate = trimToNull(value);
                if (candidate != null && !"unknown".equalsIgnoreCase(candidate)) {
                    return candidate;
                }
            }
        }
        String realIp = trimToNull(request.getHeader("X-Real-IP"));
        if (realIp != null) {
            return realIp;
        }
        return trimToNull(request.getRemoteAddr());
    }

    private String hashToken(String tokenValue) {
        String resolvedToken = trimToNull(tokenValue);
        if (resolvedToken == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(resolvedToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                builder.append(String.format(Locale.ROOT, "%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
