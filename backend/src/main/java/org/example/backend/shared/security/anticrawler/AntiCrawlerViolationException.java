package org.example.backend.shared.security.anticrawler;

/**
 * AI 索引: 反扒拦截异常。
 */
public class AntiCrawlerViolationException extends RuntimeException {

    private final int httpStatus;
    private final int responseCode;
    private final AntiCrawlerResponsePayload responsePayload;

    public AntiCrawlerViolationException(String message,
                                         int httpStatus,
                                         int responseCode,
                                         AntiCrawlerResponsePayload responsePayload) {
        super(message);
        this.httpStatus = httpStatus;
        this.responseCode = responseCode;
        this.responsePayload = responsePayload;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public AntiCrawlerResponsePayload getResponsePayload() {
        return responsePayload;
    }
}
