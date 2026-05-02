package org.example.backend.shared.security.anticrawler;

/**
 * AI 索引: 反扒请求指纹上下文。
 */
public final class RequestFingerprintHolder {

    private static final ThreadLocal<RequestFingerprint> HOLDER = new ThreadLocal<>();

    private RequestFingerprintHolder() {
    }

    public static void set(RequestFingerprint fingerprint) {
        HOLDER.set(fingerprint);
    }

    public static RequestFingerprint get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
